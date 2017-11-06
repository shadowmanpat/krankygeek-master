package es.no2.rtcapp;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Logger;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class MainActivity extends AppCompatActivity {


    private static String SIGNALING_URI = "https://739.social:8080";
    //  private static final String SIGNALING_URI = "https://739.social:8080/";
//            http://192.168.122.1
    //private static final String SIGNALING_URI = "http://100.0.0.156:7000";
    private static final String VIDEO_TRACK_ID = "video1";
    private static final String AUDIO_TRACK_ID = "audio1";
    private static final String LOCAL_STREAM_ID = "stream1";
    private static final String SDP_MID = "sdpMid";
    private static final String SDP_M_LINE_INDEX = "sdpMLineIndex";
    private static final String SDP = "sdp";
    private static final String CREATEOFFER = "createoffer";
    private static final String OFFER = "offer";
    private static final String PEERS = "peers";
    private static final String ANSWER = "answer";

    private static final String STUNSERVERS = "stunservers";
    private static final String TURNSERVERS  = "turnservers";
    private static final String MESSAGE = "message";
    private static final String TYPE = "type";
    private static final String PAYLOAD = "payload";



    private static final String CANDIDATE = "candidate";

    private PeerConnectionFactory peerConnectionFactory;
    private VideoSource localVideoSource;
    private PeerConnection peerConnection;
    private MediaStream localMediaStream;
    private VideoRenderer otherPeerRenderer;
    private Socket socket;
    private boolean createOffer = false;
    private static Logger logger =  Logger.getAnonymousLogger();
    String stun="stun:stun.l.google.com:19302";
    boolean dummy=false;
    String to="";
    boolean beTheFirstOne=false;
    long sid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        methodRequiresPermission();

        if(dummy){
            SIGNALING_URI = "http://100.0.0.156:7000";
        }

    }

    private void setConnection() {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        PeerConnectionFactory.initializeAndroidGlobals(
                this,  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true,  // Hardware Acceleration Enabled
                null); // Render EGL Context

        peerConnectionFactory = new PeerConnectionFactory();

        VideoCapturerAndroid vc = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfFrontFacingDevice(), null);

        localVideoSource = peerConnectionFactory.createVideoSource(vc, new MediaConstraints());
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
        localVideoTrack.setEnabled(true);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);

        localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID);
        localMediaStream.addTrack(localVideoTrack);
        localMediaStream.addTrack(localAudioTrack);

        GLSurfaceView videoView = (GLSurfaceView) findViewById(R.id.glview_call);

        VideoRendererGui.setView(videoView, null);
        try {
            otherPeerRenderer = VideoRendererGui.createGui(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            VideoRenderer renderer = VideoRendererGui.createGui(50, 50, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
            localVideoTrack.addRenderer(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    String stunServer="";
    String turnServer="";
    ArrayList<PeerConnection.IceServer> iceServers;

    public void iniPeerConnection(){
        if(!stunServer.equals("")&&!turnServer.equals(""))

        {
            peerConnection = peerConnectionFactory.createPeerConnection(
                    iceServers,
                    new MediaConstraints(),
                    peerConnectionObserver);

            peerConnection.addStream(localMediaStream);

            socket.emit("join","tessghfdsdfagsfdaserjkldsfhwt3346");
            Log.d("state"," socket.emit(join);");
        }
    }

    public void onConnect(View button) {
        if (peerConnection != null)
            return;



        iceServers = new ArrayList<>();

        try {
            socket = IO.socket(SIGNALING_URI);
            Log.d("state", " socket.toString() "+ socket.io().toString());
            Log.d("state","socket id"+ socket.id());
            final String TAG ="state";
            socket.on(STUNSERVERS, new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.d("state",STUNSERVERS +" "+args[0]);
                    if(!dummy){

                        JSONArray array=(JSONArray) args[0];

                        try {

                            stunServer=array.getJSONObject(0).get("url").toString();
                            iceServers.add(new PeerConnection.IceServer(stunServer));
                            iniPeerConnection();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }




                    }

                }

            }).on(TURNSERVERS, new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.d("state",TURNSERVERS +" "+args[0]);
                    if(!dummy){

                        JSONArray array=(JSONArray) args[0];

                        try {

                            JSONObject o=array.getJSONObject(0);
                           String username= o.get("username").toString();
                           String credential= o.get("credential").toString();
                           turnServer= o.getJSONArray("urls").get(0).toString();

                              iceServers.add(new PeerConnection.IceServer(turnServer,username,credential));
                            iniPeerConnection();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }




                    }

                }

            })
                    .on(PEERS, new Emitter.Listener() {

                        @Override
                        public void call(Object... args) {
                            Log.d("state peers","peers"+ args[0].toString()+"my socket.id "+ socket.id());

                            try {
                                JSONArray array=new JSONArray(args[0].toString());
                                if(array.length()>0){
                                    to=array.getString(0);
                                    if(beTheFirstOne){

                                        createOffer = true;
                                        peerConnection.createOffer(sdpObserver, new MediaConstraints());

                                    }

                                    Log.d("state","Offer");
                                }else{
                                    Log.d("state","beTheFirstOne = true;");
                                    beTheFirstOne = true;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("state"," peers JSONException "+  e.toString());

                            }
                        }

                    }). on(MESSAGE, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("state","message");
                    try {
                        JSONObject message = (JSONObject) args[0];

                        to=message.getString("from");
                        String type=message.getString("type");

                        if(type.equals(OFFER))
                        {

                            Log.d("state",OFFER+"came in "+args[0]);

                            to= message.get("to").toString();
                            sid=Long.parseLong(message.get("sid").toString());

                            JSONObject payload=message.getJSONObject("payload");
                            String sSdp=payload.getString("sdp");


                            SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER,
                                    sSdp);
                            peerConnection.setRemoteDescription(sdpObserver, sdp);
                            peerConnection.createAnswer(sdpObserver, new MediaConstraints());

                        }
                        else  if(type.equals(CANDIDATE))
                        {
                            Log.d("state received",CANDIDATE+" "+args[0]);
                            JSONObject payload=message.getJSONObject("payload");
                            JSONObject candidate= payload.getJSONObject("candidate");

                            peerConnection.addIceCandidate(
                                    new IceCandidate(candidate.getString(SDP_MID),
                                            candidate.getInt(SDP_M_LINE_INDEX),
                                            candidate.getString(CANDIDATE)));

                        }else  if(type.equals(ANSWER))
                        {
                            Log.d("state received","Answer"+args[0]);
                            try {
                                JSONObject payload=message.getJSONObject("payload");
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, payload.getString(SDP));

                                peerConnection.setRemoteDescription(sdpObserver, sdp);


                            } catch (JSONException e) {
                                Log.d("state"," Answer JSONException "+  e.toString());
                                e.printStackTrace();
                            }

                        }

                    } catch (JSONException e) {
                        Log.d("state"," Answer JSONException "+  e.toString());
                        e.printStackTrace();
                    }
                }

            });
            Log.d("state","socket.connect();");
            socket.connect();



        } catch (URISyntaxException e) {

            e.printStackTrace();
            Log.d("state"," URISyntaxException "+  e.toString());
        }
    }

    SdpObserver sdpObserver = new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.d("state"," URISyntaxException onCreateSuccess ");

            peerConnection.setLocalDescription(sdpObserver, sessionDescription);


            try {
                JSONObject obj = new JSONObject();
                obj.put(SDP, sessionDescription.description);
                if (createOffer) {
                    sid=System.currentTimeMillis();
                    if(!dummy){
                        JSONObject message= new JSONObject();
                        message.put("to", to);
                        message.put("sid", sid);
                        message.put("roomType","video");
                        message.put(TYPE,OFFER);
                        message.put(PAYLOAD,obj);
                        message.put("prefix", "webkit");
                        socket.emit(MESSAGE, message);
                        Log.d("state","Offer"+message.toString());
                    }
                    else
                    {
                        Log.d("state","createOffer "+createOffer +obj.toString());
                        socket.emit(OFFER, obj);
                    }

                } else {
                    if(!dummy){
                        JSONObject message= new JSONObject();
                        message.put("to", to);
                        message.put("roomType","video");
                        message.put("sid", sid);
                        message.put(TYPE,ANSWER);
                        message.put(PAYLOAD,obj);
                        message.put("prefix", "webkit");
                        socket.emit(MESSAGE, message);
                        Log.d("state answer"," message"+message.toString());
                    }
                    else
                    {
                        Log.d("state","createOffer "+createOffer +obj.toString());
                        socket.emit(ANSWER, obj);
                    }

                }
            } catch (JSONException e) {
                Log.d("state"," (nCreateSuccess JSONException e) "+  e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {
            Log.d("state"," (onSetSuccess()");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.d("state","onCreateFailure(String s) ");
        }

        @Override
        public void onSetFailure(String s) {
            Log.d("state","onSetFailure");
        }
    };

    PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d("RTCAPP", "onSignalingChange:" + signalingState.toString());
            Log.d("state", "onSignalingChange:" + signalingState.toString());
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d("RTCAPP", "onIceConnectionChange:" + iceConnectionState.toString());
            Log.d("state", "onIceConnectionChange:" + iceConnectionState.toString());
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.d("state", "nIceConnectionReceivingChange(boolean b)" +b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {

            try {

                JSONObject sdp = new JSONObject();
                sdp.put(SDP_MID, iceCandidate.sdpMid);
                sdp.put(SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);
                sdp.put(SDP, iceCandidate.sdp);


                if(!dummy){
                    JSONObject candidate = new JSONObject();
                    candidate.put(SDP_MID, iceCandidate.sdpMid);
                    candidate.put(SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);
                    candidate.put(CANDIDATE, iceCandidate.sdp);

                    JSONObject payload = new JSONObject();
                    payload.put(CANDIDATE,candidate);

                    JSONObject message = new JSONObject();
                    message.put("to",to);
                    message.put("sid",sid);
                    message.put("roomType","video");
                    message.put(TYPE,CANDIDATE);
                    message.put(PAYLOAD, payload);
                    message.put("prefix", "webkit");

                    socket.emit(MESSAGE, message);
                    Log.d("state", "socket.emit(MESSAGE, message);" +message);
                }
                else{

                    socket.emit(CANDIDATE, sdp);
                }

            } catch (JSONException e) {
                Log.d("state", "onIceGatheringChange JSONException e"+e.toString() );
                e.printStackTrace();
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            mediaStream.videoTracks.getFirst().addRenderer(otherPeerRenderer);
           // {"to":"Ainov1q3yICTvy_jAAAU","sid":"1506585430639","roomType":"video","type":"endOfCandidates","prefix":"webkit"}
            JSONObject endOfCandidates = new JSONObject();
            try {
                endOfCandidates.put("to",to);
                endOfCandidates.put("sid",sid);
                endOfCandidates.put("type","endOfCandidates");
                endOfCandidates.put("prefix","webkit");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("state", "onAddStream(" );
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d("state", "onRemoveStream" );
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.d("state", " onDataChannel(" );
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d("state", " onRenegotiationNeeded" );
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @AfterPermissionGranted(1)
    private void methodRequiresPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            setConnection();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this,"Give Permisson",
                    1, perms);
        }


    }
}