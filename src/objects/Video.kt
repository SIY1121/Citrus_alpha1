package objects

import annotation.CObject
import annotation.CProperty
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import ui.GlCanvas
import util.Statics
import java.nio.ByteBuffer
import java.nio.IntBuffer
import javax.imageio.ImageIO
import javafx.embed.swing.SwingFXUtils
import java.awt.image.RenderedImage
import java.io.File



@CObject("動画")
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

    init {
        file.listener = this
    }

    override fun onChanged(file: String) {
        println("video $file")
        grabber = FFmpegFrameGrabber(file)
        grabber?.start()
        isGrabberStarted = true
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
            it.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, grabber?.imageWidth ?: 0, grabber?.imageHeight?: 0, 0, GL.GL_BGR, GL.GL_UNSIGNED_BYTE, ByteBuffer.allocate( (grabber?.imageWidth?:0) *  (grabber?.imageHeight?:0) * 3))
            println("allocate ${grabber?.imageWidth}x${grabber?.imageHeight}")
            false
        })
    }

    override fun onDraw(gl: GL2, mode: DrawMode) {
        super.onDraw(gl, mode)

        if (isGrabberStarted){

            gl.glBindTexture(GL.GL_TEXTURE_2D, textureID)

            if (oldFrame != frame) {
                val now = (frame * (1.0/Statics.project.fps) * 1000 * 1000).toLong()

                if (Math.abs(frame - oldFrame) > 30)
                    grabber?.timestamp = now

                while (grabber?.timestamp?:0 < now || buf?.image==null)
                    buf = grabber?.grabFrame()


                println("video frame $frame ${grabber?.timestamp} $now ${buf?.image?.get(0)==null}")
                gl.glTexSubImage2D(GL.GL_TEXTURE_2D,0,0,0,buf?.imageWidth?:0,buf?.imageHeight?:0,GL.GL_BGR,GL2.GL_UNSIGNED_BYTE,buf?.image?.get(0))
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
}