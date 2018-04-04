package util

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import org.bytedeco.javacv.FFmpegFrameFilter
import org.bytedeco.javacv.FFmpegFrameRecorder
import ui.FFmpegFrameFilterMod
import ui.GlCanvas
import java.io.File
import java.nio.ByteBuffer
import java.nio.IntBuffer

class VideoRenderer {
    companion object {
        var time = 0L
        lateinit var recorder: FFmpegFrameRecorder
        //lateinit var filter: FFmpegFrameFilterMod
        fun startEncode() {
            recorder = FFmpegFrameRecorder(File("out.mp4"), Statics.project.width, Statics.project.height)

            time = System.currentTimeMillis()
            recorder.start()
            GlCanvas.instance.animator.stop()
            GlCanvas.instance.currentFrame = 0
            GlCanvas.instance.rendering = true
            //TODO テクスチャ反転問題をプレビュー時に反転して、出力時は普通に出すべきか
            //filter = FFmpegFrameFilterMod("vflip", Statics.project.width, Statics.project.height)
            //filter.start()
            while (GlCanvas.instance.currentFrame < 260) {
                GlCanvas.instance.display()
                GlCanvas.instance.currentFrame++
            }

            endEncode()
            GlCanvas.instance.rendering = false
        }

        fun recordFrame(buf: ByteBuffer) {
            //filter.pushImage(Statics.project.width, Statics.project.height, 8, 3, Statics.project.width * 3, -1, buf)
            //recorder.record(filter.pullImage(), 3)
            recorder.recordImage(Statics.project.width, Statics.project.height, 8, 3, Statics.project.width * 3, -1, buf)
        }

        fun endEncode() {
            //filter.stop()
            recorder.stop()
            recorder.release()
            println(System.currentTimeMillis() - time)
        }
    }
}