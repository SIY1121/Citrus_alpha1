package ui

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.awt.GLJPanel
import com.jogamp.opengl.util.FPSAnimator

class GlCanvas:GLJPanel(),GLEventListener {

    val animator = FPSAnimator(60)
    lateinit var gl : GL2

    init{
        addGLEventListener(this)
    }

    override fun init(p0: GLAutoDrawable) {
        gl = p0.gl.gL2
        gl.glClearColor(1f,0f,0f,1f)
        animator.add(p0)
        animator.start()
    }
    override fun reshape(p0: GLAutoDrawable?, p1: Int, p2: Int, p3: Int, p4: Int) {

    }

    override fun display(p0: GLAutoDrawable?) {
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT)
    }

    override fun dispose(p0: GLAutoDrawable?) {

    }

}