package objects

import annotation.CDroppable
import annotation.CObject
import annotation.CProperty
import com.jogamp.openal.AL
import com.jogamp.openal.ALFactory
import com.jogamp.openal.util.ALut
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import kotlinx.coroutines.experimental.launch
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import properties.FileProperty
import properties.MutableProperty
import ui.DialogFactory
import util.Statics
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

@CObject("音声")
@CDroppable(["ac3", "aac", "adts", "aif", "aiff", "afc", "aifc", "amr", "au", "bit", "caf", "dts", "eac3", "flac", "g722", "tco", "rco", "gsm", "lbc", "latm", "loas", "mka", "mp2", "m2a", "mpa", "mp3", "oga", "oma", "opus", "spx", "tta", "voc", "wav", "wv"])
class Audio : CitrusObject(), FileProperty.ChangeListener {

    @CProperty("ファイル", 0)
    val file = FileProperty(listOf())

    @CProperty("音量", 1)
    val volume = MutableProperty(0.0, 1.0, 0.0, 1.0, 1.0)

    var grabber: FFmpegFrameGrabber? = null
    var isGrabberStarted = false

    var audioLine: SourceDataLine? = null

    var oldFrame = -100
    var buf: Frame? = null

    var audioLength = 0

    //val al: AL
    //val bufCount = 2
    //val buffers = IntArray(bufCount)
    //val sources = IntArray(1)

    //var first = true

    init {
        file.listener = this
//        ALut.alutInit()
//        al = ALFactory.getAL()
//        println("al error i : " + al.alGetError())
//        al.alGenBuffers(bufCount, buffers, 0)
//        al.alGenSources(1, sources, 0)
//        al.alSourcef(sources[0], AL.AL_PITCH, 1.0f)
//        al.alSourcef(sources[0], AL.AL_GAIN, 1.0f)
//        al.alSourcefv(sources[0], AL.AL_POSITION, floatArrayOf(0f, 0f, 0f), 0)
//        al.alSourcefv(sources[0], AL.AL_VELOCITY, floatArrayOf(0f, 0f, 0f), 0)
//        al.alSourcei(sources[0],AL.AL_LOOPING,AL.AL_FALSE)
//        al.alListenerfv(AL.AL_POSITION,	floatArrayOf(0f, 0f, 0f), 0)
//        al.alListenerfv(AL.AL_VELOCITY,    floatArrayOf(0f, 0f, 0f), 0)
//        al.alListenerfv(AL.AL_ORIENTATION, floatArrayOf( 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f), 0)
//        println("al error : " + al.alGetError())
    }

    override fun onFileDropped(file: String) {
        onChanged(file)
    }

    override fun onChanged(file: String) {
        val dialog = DialogFactory.buildOnProgressDialog("処理中", "音声を読み込み中...")
        dialog.show()
        launch{
            grabber = FFmpegFrameGrabber(file)
            grabber?.start()
            if (grabber?.videoCodec == 0) {
                Platform.runLater {
                    dialog.close()
                    val alert = Alert(Alert.AlertType.ERROR, "音声コーデックを識別できませんでした", ButtonType.CLOSE)
                    alert.headerText = null
                    alert.showAndWait()
                }
                return@launch
            }
            audioLength = ((grabber?.lengthInFrames ?: 1) * (Statics.project.fps / (grabber?.frameRate
                    ?: 30.0))).toInt()
            end = start + audioLength
            //オーディオ出力準備
            val audioFormat = AudioFormat((grabber?.sampleRate?.toFloat() ?: 0f), 16, 2, true, true)

            val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
            audioLine = AudioSystem.getLine(info) as SourceDataLine
            audioLine?.open(audioFormat)
            audioLine?.start()
            isGrabberStarted = true

            Platform.runLater {
                uiObject?.onScaleChanged()
                dialog.close()
                displayName = "音声 $file"
            }
        }

    }

    override fun onLayoutUpdate() {
        if (end - start > audioLength)
            end = start + audioLength
        uiObject?.onScaleChanged()
    }

    override fun onFrame() {
        if (isGrabberStarted) {
            if (oldFrame != frame) {
                val now = ((frame + 5) * (1.0 / Statics.project.fps) * 1000 * 1000).toLong()

                if (Math.abs(frame - oldFrame) > 30)
                    grabber?.timestamp = now

                while (grabber?.timestamp ?: 0 <= now) {
                    // println("a:" + grabber?.timestamp + " ")
                    if (buf?.samples != null) {

                        val s = (buf?.samples?.get(0) as ShortBuffer)
                        val arr = s.toByteArray()
                        audioLine?.write(arr, 0, arr.size)
                        println(audioLine?.framePosition)
                    }

//                    if (buf?.samples != null) {
//                        val s = (buf?.samples?.get(0) as ShortBuffer)
//                        val arr = s.toByteArray()
//                        if (first) {
//                            for(i in 0 until bufCount)
//                                 al.alBufferData(buffers[i], AL.AL_FORMAT_STEREO16, ByteBuffer.wrap(arr), arr.size, grabber?.sampleRate
//                                    ?: 44100)
//
//                            println("al error 1 : " + al.alGetError())
//                            al.alSourceQueueBuffers(sources[0], bufCount, buffers, 0)
//
//                            println("al error 11 : " + al.alGetError())
//                            al.alSourcePlay(sources[0])
//
//                            println("al error 12 : " + al.alGetError())
//                            first = false
//                        } else {
//                            val processed = IntArray(1)
//                            val tmpBuffer = IntArray(1)
//                            al.alGetSourcei(sources[0], AL.AL_BUFFERS_PROCESSED, processed, 0)
//                            println(processed[0])
//                            if (processed[0] > 0) {
//                                al.alSourceUnqueueBuffers(sources[0], 1, tmpBuffer, 0)
//                                println("tmp buf " + tmpBuffer[0])
//                                al.alBufferData(tmpBuffer[0], AL.AL_FORMAT_STEREO16, ByteBuffer.wrap(arr), arr.size, grabber?.sampleRate
//                                        ?: 44100)
//                                al.alSourceQueueBuffers(sources[0], 1, tmpBuffer, 0)
//                            }
//                            al.alSourcePlay(sources[0])
//                            println("al error 2 : " + al.alGetError())
//                        }
//                    }


                    buf = grabber?.grabSamples()
                }

            }
            oldFrame = frame
        }
    }

    fun ShortBuffer.toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(this.limit() * 2)
        val shortArray = ShortArray(this.limit())
        this.get(shortArray)

        byteBuffer.asShortBuffer().put(shortArray.map { (it * volume.value(frame)).toShort() }.toShortArray())
        return byteBuffer.array()
    }
}