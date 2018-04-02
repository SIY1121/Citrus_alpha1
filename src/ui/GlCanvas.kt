package ui

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.awt.GLJPanel
import com.jogamp.opengl.glu.GLU
import com.jogamp.opengl.util.FPSAnimator
import objects.CitrusObject
import util.Statics

class GlCanvas : GLJPanel(), GLEventListener {

    var currentFrame = 0
        set(value) {
            field = value
            for (i in 0 until Statics.project.Layer.size) {
                //i番目のレイヤーで、前回のオブジェクトの範囲を外れた場合か、そもそもなかった場合
                //新たなオブジェクトを検索
                if (currentObjects[i]?.isActive(field) != true) {
                    currentObjects.remove(i)
                    //println("remove$i")
                    for (o in Statics.project.Layer[i]) {
                        //新たなオブジェクトを見つけた場合
                        //セット
                        if (o.isActive(field)) {
                            currentObjects[i] = o
                            break
                        }
                    }
                }
            }
        }


    val animator = FPSAnimator(60)

    companion object {
        lateinit var instance : GlCanvas
        lateinit var gl2: GL2
        val glu = GLU()
    }

    val currentObjects: HashMap<Int, CitrusObject> = HashMap()

    init {
        instance = this
        addGLEventListener(this)
    }

    override fun init(p0: GLAutoDrawable) {
        println("init")
        gl2 = p0.gl.gL2
        gl2.glEnable(GL2.GL_TEXTURE_2D)
        gl2.glClearColor(1f, 0f, 0f, 1f)

        animator.add(p0)
        animator.start()
    }

    override fun reshape(p0: GLAutoDrawable?, p1: Int, p2: Int, p3: Int, p4: Int) {
        gl2.glMatrixMode(GL2.GL_PROJECTION)
        gl2.glLoadIdentity()
        //gl2.glOrtho(-Statics.project.width/2.0,Statics.project.width/2.0,-Statics.project.height/2.0,Statics.project.height/2.0,0.1,100.0)
        glu.gluPerspective(90.0, Statics.project.width.toDouble() / Statics.project.height, 1.0, Statics.project.width.toDouble())
        glu.gluLookAt(0.0, 0.0, Statics.project.height / 2.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
    }

    override fun display(p0: GLAutoDrawable?) {

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT)
        gl2.glMatrixMode(GL2.GL_MODELVIEW)
        gl2.glLoadIdentity()
        //println(currentObjects.size)
        for (o in currentObjects)
            o.value.onSuperFrame(currentFrame)

        
    }

    override fun dispose(p0: GLAutoDrawable?) {

    }

}