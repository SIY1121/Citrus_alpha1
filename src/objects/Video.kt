package objects

import annotation.CDroppable
import annotation.CObject
import annotation.CProperty
import com.jogamp.common.util.Bitstream
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import jogamp.opengl.util.av.impl.FFMPEGMediaPlayer
import kotlinx.coroutines.experimental.launch
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.OpenCVFrameGrabber
import ui.GlCanvas
import util.Statics
import java.nio.ByteBuffer
import java.nio.IntBuffer
import properties.FileProperty
import properties.SwitchableProperty
import ui.DialogFactory
import ui.TimelineController
import util.SerializedOperationQueue
import java.io.*
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine


@CObject("動画")
@CDroppable(["asf","wmv","wma","asf","wmv","wma","avi","flv","h261","h263","m4v","m4a","ismv","isma","mkv","mjpg","mjpeg","mp4","mpg","mpeg","mpg","mpeg","m1v","dvd","vob","vob","ts","m2t","m2ts","mts","nut","ogv","webm","chk"])
class Video : DrawableObject(), FileProperty.ChangeListener {

    override val id = "citrus/video"
    override val name = "動画"

    @CProperty("ファイル", 0)
    val file = FileProperty(listOf())

    var grabber: FFmpegFrameGrabber? = null
    var isGrabberStarted = false

    var oldFrame = -100
    var buf: Frame? = null

    var textureID: Int = 0

    var videoLength = 0

    init {
        file.listener = this

    }

    override fun onFileDropped(file: String) {
        onChanged(file)
        TimelineController.instance.addObject(Audio::class.java, layer + 1, file)
    }

    override fun onChanged(file: String) {
        val dialog = DialogFactory.buildOnProgressDialog("処理中", "動画を読み込み中...")
        dialog.show()
        launch {
            //デコーダ準備
            grabber = FFmpegFrameGrabber(file)
            grabber?.timestamp
            grabber?.start()
            if(grabber?.videoCodec==0){
                Platform.runLater {
                    val alert = Alert(Alert.AlertType.ERROR,"動画コーデックを識別できませんでした", ButtonType.CLOSE)
                    alert.headerText = null
                    dialog.close()
                    alert.showAndWait()
                }
                return@launch
            }
            isGrabberStarted = true
            videoLength = ((grabber?.lengthInFrames ?: 1) * (Statics.project.fps / (grabber?.frameRate
                    ?: 30.0))).toInt()
            end = start + videoLength
            //テクスチャ準備
            GlCanvas.instance.invoke(true, {
                if (textureID != 0) {
                    val b = IntBuffer.allocate(1)
                    b.put(textureID)
                    it.gl.glDeleteTextures(GL.GL_TEXTURE_2D, b)
                }
                val b = IntBuffer.allocate(1)
                it.gl.glGenTextures(1, b)
                textureID = b.get()
                println("textureID : $textureID")
                it.gl.glBindTexture(GL.GL_TEXTURE_2D, textureID)
                it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
                it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
                it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
                it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
                it.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, grabber?.imageWidth ?: 0, grabber?.imageHeight
                        ?: 0, 0, GL.GL_BGR, GL.GL_UNSIGNED_BYTE, ByteBuffer.allocate((grabber?.imageWidth
                        ?: 0) * (grabber?.imageHeight ?: 0) * 3))
                println("allocate ${grabber?.imageWidth}x${grabber?.imageHeight}")
                false
            })
            Platform.runLater {
                dialog.close()
                uiObject?.onScaleChanged()
                displayName = "動画 $file"
            }
        }
    }

    override fun onLayoutUpdate() {
        if(end - start > videoLength)
            end = start + videoLength
        uiObject?.onScaleChanged()
    }

    override fun onDraw(gl: GL2, mode: DrawMode) {
        super.onDraw(gl, mode)

        if (isGrabberStarted) {
            gl.glBindTexture(GL.GL_TEXTURE_2D, textureID)

            //フレームが変わった場合にのみ処理
            if (oldFrame != frame) {
                val now = (frame * (1.0 / Statics.project.fps) * 1000 * 1000).toLong()

                //移動距離が30フレーム以上でシーク処理を実行
                if (Math.abs(frame - oldFrame) > 30 || frame < oldFrame) {
                    TimelineController.wait = true
                    grabber?.timestamp = now - 10000
                    TimelineController.wait = false
                    //buf = grabber?.grabFrame()
                }
                //buf = null
                //画像フレームを取得できており、タイムスタンプが理想値より上回るまでループ
                while (grabber?.timestamp ?: 0 <= now) {
                    buf = grabber?.grabImage()
                }
                gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, buf?.imageWidth ?: 0, buf?.imageHeight
                        ?: 0, GL.GL_BGR, GL2.GL_UNSIGNED_BYTE, buf?.image?.get(0))
            }

            gl.glBegin(GL2.GL_QUADS)
            gl.glTexCoord2d(0.0, 1.0)
            gl.glVertex3d(-(buf?.imageWidth ?: 0) / 2.0, -(buf?.imageHeight ?: 0) / 2.0, 0.0)
            gl.glTexCoord2d(0.0, 0.0)
            gl.glVertex3d(-(buf?.imageWidth ?: 0) / 2.0, (buf?.imageHeight ?: 0) / 2.0, 0.0)
            gl.glTexCoord2d(1.0, 0.0)
            gl.glVertex3d((buf?.imageWidth ?: 0) / 2.0, (buf?.imageHeight ?: 0) / 2.0, 0.0)
            gl.glTexCoord2d(1.0, 1.0)
            gl.glVertex3d((buf?.imageWidth ?: 0) / 2.0, -(buf?.imageHeight ?: 0) / 2.0, 0.0)
            gl.glEnd()
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0)

            oldFrame = frame
        }
    }

    fun ShortBuffer.toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(this.limit() * 2)
        val shortArray = ShortArray(this.limit())
        this.get(shortArray)
        byteBuffer.asShortBuffer().put(shortArray)
        return byteBuffer.array()
    }

}