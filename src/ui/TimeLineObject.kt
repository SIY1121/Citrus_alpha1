package ui

import annotation.CObject
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import objects.CitrusObject
import annotation.CProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import interpolation.LinearInterpolation
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import objects.MutableProperty
import objects.SelectableProperty
import util.Settings


class TimeLineObject(var cObject: CitrusObject,val timelineController: TimelineController) : VBox(),
        CitrusObject.DisplayNameChangeListener {

    /**
     * プロパティとUIを一括管理するためのクラス
     */
    data class PropertyData(val kProprety : KProperty1<CitrusObject,*>, var proprety : Any?, var node : Node?)

    /**
     * セクション名とプロパティのリストを持つデータクラス
     * @param group グループ名
     * @param property 初期化するリスト
     */
    data class PropertySection(val group: String, val property: MutableList<PropertyData> = ArrayList())

    /**
     * 表示用ラベル
     */
    val label = Label()

    /**
     * 編集コントロールを表示するウィンドウ
     */
    val popup = PopupControl()

    /**
     * ポップアップウィンドウのルート要素
     */
    lateinit var popupRoot: VBox

    /**
     * セクションのリスト
     */
    val properties: MutableList<PropertySection> = ArrayList()

    private var currentFrame = 0

    /**
     * カーソルの位置から
     * 編集モードを変更、通知する
     */
    private val mouseMove = EventHandler<MouseEvent> {
        when {
            it.x < 5 -> {
                scene.cursor = Cursor.H_RESIZE
                editModeChangeListener?.onEditModeChanged(EditMode.DecrementLength, it.x, it.y)
            }
            it.x > width - 5 -> {
                scene.cursor = Cursor.H_RESIZE
                editModeChangeListener?.onEditModeChanged(EditMode.IncrementLength, it.x, it.y)
            }
            else -> {
                scene.cursor = Cursor.DEFAULT
                editModeChangeListener?.onEditModeChanged(EditMode.Move, it.x, it.y)
            }
        }
    }

    /**
     * カーソルが出た場合に
     * 標準に戻す
     */
    private val mouseExited = EventHandler<MouseEvent> {
        scene.cursor = Cursor.DEFAULT
    }

    /**
     * クリック時の動作
     */
    private val mouseClicked = EventHandler<MouseEvent> {
        //ポップアップモード
        if(Settings.popupEditWindow){
            if(it.button == MouseButton.SECONDARY){
                popup.show(this, it.screenX, it.screenY)

            }else{
                popup.hide()
            }
            it.consume()
        }else{
            //サイドバーモード
            if(it.button == MouseButton.PRIMARY){
                timelineController.parentController.rightPane.children.clear()
                timelineController.parentController.rightPane.children.add(popupRoot)
                AnchorPane.setRightAnchor(popupRoot,0.0)
                AnchorPane.setLeftAnchor(popupRoot,0.0)
            }
        }
    }

    init {
        cObject.displayNameChangeListener = this
        onMouseMoved = mouseMove
        onMouseExited = mouseExited
        onMouseClicked = mouseClicked
        label.maxWidthProperty().bind(widthProperty())
        children.add(label)

        popupRoot = VBox()
        popupRoot.style = "-fx-base: #323232;-fx-background-color:#383838AA;-fx-border-radius: 5 5 5 5;-fx-background-radius: 5 5 5 5;"
        popupRoot.effect = DropShadow()
        popupRoot.padding = Insets(10.0)


        //cObjectの親クラスをたどり、それぞれのプロパティを取得
        cObject.javaClass.kotlin.allSuperclasses.reversed().forEach { clazz ->
            //これらのクラスは除外
            if (clazz != Any::class && clazz != CitrusObject::class) {
                val section = PropertySection(
                        if (clazz.annotations.isNotEmpty() && clazz.annotations[0] is CObject)
                            (clazz.annotations[0] as CObject).name
                        else
                            "無題"
                )
                //CPropertyアノテーションを持ったプロパティのみ登録
                clazz.memberProperties.filter { it.annotations.isNotEmpty() && it.annotations[0] is CProperty }
                        .forEach { p ->
                            section.property.add(PropertyData(cObject.javaClass.kotlin.memberProperties.first { p.name == it.name },null,null))
                        }
                properties.add(section)
            }
        }

        //cObject本体のプロパティを登録
        val section = PropertySection(
                if (cObject.javaClass.kotlin.annotations.isNotEmpty() && cObject.javaClass.kotlin.annotations[0] is CObject)
                    (cObject.javaClass.kotlin.annotations[0] as CObject).name
                else
                    "無題")
        cObject.javaClass.kotlin.declaredMemberProperties.filter { it.annotations.isNotEmpty() && it.annotations[0] is CProperty }
                .forEach {
                    section.property.add(PropertyData(it,null,null))
                }
        properties.add(section)

        //取得したプロパティからUIを生成
        for (p in properties) {
            val grid = GridPane()
            val accordion = TitledPane(p.group, grid)
            grid.columnConstraints.add(ColumnConstraints())
            grid.columnConstraints.add(ColumnConstraints())
            grid.prefWidthProperty().bind(accordion.widthProperty())
            accordion.isAnimated = false

            //CPropertyアノテーションのindexに基づいてソート
            p.property.sortWith(Comparator { o1, o2 -> (o1.kProprety.annotations[0] as CProperty).index - (o2.kProprety.annotations[0] as CProperty).index })

            for ((i,pp) in p.property.withIndex()) {
                val name = (pp.kProprety.annotations[0] as CProperty).displayName
                val v = pp.kProprety.get(cObject)
                pp.proprety = v
                when (v) {
                    is MutableProperty -> {

                        grid.add(Label(name),0,i)
                        val slider = Slider()
                        slider.min = v.min
                        slider.max = v.max
                        slider.value = v.value(1)
                        slider.valueProperty().addListener({ _, _, n ->
                            //キーフレームがない場合
                            if(v.keyFrames.size==1){
                                v.keyFrames[0].value = n.toDouble()
                            }
                        })
                        slider.setOnKeyPressed {
                            if(it.code == KeyCode.I){
                                println("added:${v.getKeyFrameIndex(currentFrame)+1},$currentFrame ,${slider.value}")
                                v.keyFrames.add(v.getKeyFrameIndex(currentFrame)+1, MutableProperty.KeyFrame(currentFrame,LinearInterpolation(),slider.value))
                                println(v.keyFrames.last().value)
                            }
                        }
                        GridPane.setMargin(slider, Insets(5.0))
                        grid.add(slider,1,i)
                        pp.node = slider
                    }
                    is SelectableProperty -> {
                        grid.add(Label(name),0,i)
                        val choice = ChoiceBox<String>()
                        choice.items.addAll(v.list)
                        choice.setOnAction { v.selectedIndex = choice.selectionModel.selectedIndex }
                        grid.add(choice,1,i)


                    }
                }

            }
            grid.columnConstraints[1].hgrow=Priority.ALWAYS
            popupRoot.children.add(accordion)
        }



        if (Settings.popupEditWindow){
            val b = Button("OK")
            b.setOnAction { popup.hide() }
            popupRoot.children.add(b)
            popup.scene.root = popupRoot
        }

    }

    override fun onDisplayNameChanged(name: String) {
        label.text = name
    }

    enum class EditMode {
        None, Move, IncrementLength, DecrementLength
    }

    interface EditModeChangeListener {
        fun onEditModeChanged(mode: EditMode, offsetX: Double, offsetY: Double)
    }

    var editModeChangeListener: EditModeChangeListener? = null

    fun onLayerChanged(old: Int, new: Int) {
        cObject.layer = new
    }

    fun onMoved() {
        cObject.start = (layoutX / TimelineController.pixelPerFrame).toInt()
        cObject.end = ((layoutX + width) / TimelineController.pixelPerFrame).toInt()
        //println("${Statics.project.Layer[cObject.layer].indexOf(cObject)}/${Statics.project.Layer[cObject.layer].size-1}")
        cObject.onLayoutUpdate()
    }

    fun onCaretChanged(frame : Int){
        currentFrame = frame - cObject.start
        println((properties.first().property.first().proprety as MutableProperty).getKeyFrameIndex(currentFrame))
        for(ps in properties)
            for(p in ps.property)
            {
                val pro = p.proprety
                when(pro){
                    is MutableProperty->{
                        (p.node as Slider).value = pro.value(currentFrame)
                    }
                }
            }
    }

    fun onScaleChanged() {
        layoutX = cObject.start * TimelineController.pixelPerFrame
        prefWidth = cObject.end * TimelineController.pixelPerFrame - layoutX
    }

}