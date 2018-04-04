package objects

import annotation.CObject
import annotation.CProperty
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import javafx.scene.SnapshotParameters
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.text.Font
import properties.ColorProperty
import properties.SelectableProperty
import properties.SwitchableProperty
import properties.TextProperty
import ui.GlCanvas
import java.awt.GraphicsEnvironment
import java.nio.ByteBuffer
import java.nio.IntBuffer

@CObject("テキスト")
class Text : DrawableObject(){
    override val id = "citrus/text"
    override val name = "テキスト"


    private val onTextChanged = object : TextProperty.ChangeListener{
        override fun onChanged(text: String) {
            UpdateTexture()
        }
    }

    private val onFontChanged = object : SelectableProperty.ChangeListener{
        override fun onChanged(index: Int) {
            UpdateTexture()
        }
    }

    private val onColorChanged = object : ColorProperty.ChangeListener{
        override fun onChanged(color: Color) {
            UpdateTexture()
        }
    }

    private val onIsStrokeChanged = object: SwitchableProperty.ChangeListener{
        override fun onChanged(value: Boolean) {
            UpdateTexture()
        }
    }

    @CProperty("フォント",0)
    val font = SelectableProperty(GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.map { it.fontName })

    @CProperty("文字色",1)
    val color = ColorProperty()

    @CProperty("縁取り",2)
    val isStroke = SwitchableProperty(false)

    @CProperty("縁取り色",3)
    val strokeColor = ColorProperty(Color.RED)

    @CProperty("テキスト",4)
    val text = TextProperty()


    val t = javafx.scene.text.Text("text")
    var textureID : Int = 0

    init{
        text.listener = onTextChanged
        font.listener = onFontChanged
        color.listener = onColorChanged
        isStroke.listener = onIsStrokeChanged
        strokeColor.listener = onColorChanged
        GlCanvas.instance.invoke(true,{
            val b = IntBuffer.allocate(1)
            it.gl.glGenTextures(1, b)
            textureID = b.get()
            it.gl.glBindTexture(GL.GL_TEXTURE_2D, textureID)
            it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)
            it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR)
            it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
            it.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)

            false
        })
    }
    //TODO なんか反映が不安定
    fun UpdateTexture(){
        t.text = text.text
        t.style = "-fx-background-color:transparent"
        t.fill = color.color
        t.font = Font(font.list[font.selectedIndex],200.0)
        if(isStroke.value){
            t.strokeWidth = 5.0
            t.stroke = strokeColor.color
        }

        if(t.layoutBounds.width <= 0 || t.layoutBounds.height <= 0){
            println("w h 0 ")
            return
        }

        val w = WritableImage(t.layoutBounds.width.toInt(),t.layoutBounds.height.toInt())
        val params = SnapshotParameters()
        params.fill = Color.TRANSPARENT
        t.snapshot(params,w)

        val buf = ByteBuffer.allocate(t.layoutBounds.width.toInt() * t.layoutBounds.height.toInt() * 4)

        w.pixelReader.getPixels(0,0,w.width.toInt(),w.height.toInt(), PixelFormat.getByteBgraInstance(),buf,t.layoutBounds.width.toInt()*4)

        GlCanvas.instance.invoke(true,{
            it.gl.glBindTexture(GL.GL_TEXTURE_2D,textureID)
            it.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,t.layoutBounds.width.toInt(), t.layoutBounds.height.toInt()
                    , 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, buf)
            it.gl.glBindTexture(GL.GL_TEXTURE_2D,0)
            false
        })
        displayName = "[テキスト] ${text.text.replace("\n"," ")}"
    }

    override fun onDraw(gl: GL2, mode: DrawMode) {
        super.onDraw(gl, mode)



        gl.glBindTexture(GL.GL_TEXTURE_2D,textureID)
        gl.glBegin(GL2.GL_QUADS)
        gl.glTexCoord2d(0.0, 1.0)
        gl.glVertex3d(-t.layoutBounds.width/2.0,-t.layoutBounds.height/2.0,0.0)
        gl.glTexCoord2d(0.0, 0.0)
        gl.glVertex3d(-t.layoutBounds.width/2.0,t.layoutBounds.height/2.0,0.0)
        gl.glTexCoord2d(1.0, 0.0)
        gl.glVertex3d(t.layoutBounds.width/2.0,t.layoutBounds.height/2.0,0.0)
        gl.glTexCoord2d(1.0, 1.0)
        gl.glVertex3d(t.layoutBounds.width/2.0,-t.layoutBounds.height/2.0,0.0)
        gl.glEnd()
        gl.glBindTexture(GL.GL_TEXTURE_2D,0)
    }
}