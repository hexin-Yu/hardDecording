package csu.liutao.ffmpegdemo.opgls.renders

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES30.*
import android.os.Handler
import android.view.Surface
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.programs.CameraProgram

class CameraRender(val context: Context) : GLSurfaceView.Renderer {
    private var program = CameraProgram()
    private var textureId = -1
    private lateinit var surfaceTexture: SurfaceTexture
    val handler = Handler()

    private var cameraDevice: CameraDevice? = null
    private var cameraSession: CameraCaptureSession? = null

    var listener : SurfaceTexture.OnFrameAvailableListener? = null

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        program.onDrawFrame()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        surfaceTexture.setDefaultBufferSize(width, height)
        program.onSurfaceChanged(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1f, 1f, 1f, 0f)
        textureId = GlUtils.loadExternTextureId()
        surfaceTexture = SurfaceTexture(textureId)
        program.onSurfaceCreated(context, surfaceTexture, textureId)
        initCamera()
    }

    @SuppressLint("MissingPermission")
    private fun initCamera() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        manager.openCamera(CameraCharacteristics.LENS_FACING_FRONT.toString(), object : CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createCameraSession()
            }
            override fun onDisconnected(camera: CameraDevice) = Utils.log("disconnected")
            override fun onError(camera: CameraDevice, error: Int) = Utils.log("open error")
        }, handler)
    }

    private fun createCameraSession() {
        val list = ArrayList<Surface>(1)
        val surface = Surface(surfaceTexture)
        list.add(surface)
        cameraDevice!!.createCaptureSession(list, object : CameraCaptureSession.StateCallback(){
            override fun onConfigureFailed(session: CameraCaptureSession) = Utils.log("seesion failed")

            override fun onConfigured(session: CameraCaptureSession) {
                cameraSession = session
                val builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                builder.addTarget(surface)
                if (listener != null) surfaceTexture.setOnFrameAvailableListener(listener!!)
                cameraSession!!.setRepeatingRequest(builder.build(), null, handler)
            }
        }, handler)
    }

    fun release() {
        cameraSession?.close()
        cameraSession == null
        cameraDevice?.close()
        cameraDevice = null
    }
}