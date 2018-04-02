package objects

import annotation.CObject
import annotation.CProperty
import javafx.application.Platform
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import properties.FileProperty
import ui.DialogFactory
import util.Statics
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

@CObject("音声")
class Audio : CitrusObject(), FileProperty.ChangeListener {

    @CProperty("ファイル",0)
    val file = FileProperty(listOf())
    var grabber : FFmpegFrameGrabber? = null
    var isGrabberStarted = false

    var audioLine : SourceDataLine? = null

    var oldFrame = -100
    var buf: Frame? = null

    init{
        file.listener = this
    }

    override fun onChanged(file: String) {
        val dialog = DialogFactory.buildOnProgressDialog("処理中","音声を読み込み中...")
        dialog.show()
        Thread({
            grabber = FFmpegFrameGrabber(file)
            grabber?.start()

            //オーディオ出力準備
            val audioFormat = AudioFormat((grabber?.sampleRate?.toFloat()?:0f),16,2,true,true)
            println("audio setup ${audioFormat.sampleRate}")
            val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
            audioLine = AudioSystem.getLine(info) as SourceDataLine
            audioLine?.open(audioFormat)
            audioLine?.start()
            isGrabberStarted = true
            Platform.runLater {
                dialog.close()
                displayName = "音声 $file"
            }
        }).start()

    }

    override fun onFrame() {
        if(isGrabberStarted){
            if(oldFrame!=frame){
                val now = (frame * (1.0/ Statics.project.fps) * 1000 * 1000).toLong()
                println("audio $now")
                if(Math.abs(frame-oldFrame)>30)
                    grabber?.timestamp = now

                while (grabber?.timestamp?:0<now || buf?.samples==null){
                    if(buf?.samples!=null){

                        val s = (buf?.samples?.get(0) as ShortBuffer)
                        val arr = s.toByteArray()
                        audioLine?.write(arr,0,arr.size)
                    }
                    buf = grabber?.grabFrame()
                }

            }
            oldFrame = frame
        }
    }

    fun ShortBuffer.toByteArray():ByteArray{
        val byteBuffer = ByteBuffer.allocate(this.limit()*2)
        val shortArray = ShortArray(this.limit())
        this.get(shortArray)
        byteBuffer.asShortBuffer().put(shortArray)
        return byteBuffer.array()
    }
}