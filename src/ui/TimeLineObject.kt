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
import interpolation.AccelerateDecelerateInterpolator
import interpolation.BounceInterpolator
import interpolation.Interpolator
import interpolation.InterpolatorManager
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import objects.MutableProperty
import objects.SelectableProperty
import util.Settings


class TimeLineObject(var cObject: CitrusObject, val timelineController: TimelineController) : VBox(),
        CitrusObject.DisplayNameChangeListener {

    /**
     * プロパティとUIを一括管理するためのクラス
     * @param kProprety リフレクションから取得したプロパティの生データ
     * @param property MutableProperty等のそのまま参照できるプロパティ
     * @param node スライダー等の値調整用UI
     * @param pane タイムライン上でキーフレームを表示する親Pane
     */
    data class PropertyData(val kProprety: KProperty1<CitrusObject, *>, var property: Any?, var node: Node?,var pane:Pane?)

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
        if (Settings.popupEditWindow) {
            if (it.button == MouseButton.SECONDARY) {
                popup.show(this, it.screenX, it.screenY)

            } else {
                popup.hide()
            }
            it.consume()
        } else {
            //サイドバーモード
            if (it.button == MouseButton.PRIMARY) {
                timelineController.parentController.rightPane.children.clear()
                timelineController.parentController.rightPane.children.add(popupRoot)
                AnchorPane.setRightAnchor(popupRoot, 0.0)
                AnchorPane.setLeftAnchor(popupRoot, 0.0)
            }
        }
    }

    init {
        cObject.displayNameChangeListener = this
        onMouseMoved = mouseMove
        onMouseExited = mouseExited
        onMouseClicked = mouseClicked
        label.maxWidthProperty().bind(widthProperty())
        label.minHeight = 30.0
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
                            section.property.add(PropertyData(cObject.javaClass.kotlin.memberProperties.first { p.name == it.name }, null, null,null))
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
                    section.property.add(PropertyData(it, null, null,null))
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

            for ((i, pp) in p.property.withIndex()) {
                val name = (pp.kProprety.annotations[0] as CProperty).displayName
                val v = pp.kProprety.get(cObject)
                pp.property = v
                when (v) {
                    is MutableProperty -> {

                        grid.add(Label(name), 0, i)
                        val slider = Slider()
                        slider.min = v.min
                        slider.max = v.max
                        slider.value = v.value(1)
                        slider.valueProperty().addListener({ _, _, n ->
                            //キーフレームがない場合
                            if (v.keyFrames.size==0) {
                                v.fixedValue = n.toDouble()
                            } else {//キーフレームがある場合
                                v.temporaryValue = n.toDouble()
                                v.temporaryMode = true
                            }
                        })
                        //キーフレーム追加用コード
                        slider.setOnKeyPressed {
                            if (it.code == KeyCode.I) {
                                println("added:${v.getKeyFrameIndex(currentFrame) + 1},$currentFrame ,${slider.value}")

                                if (v.keyFrames.size==0)//はじめてのキーフレーム追加の場合
                                {
                                    val pane = Pane()
                                    pane.minHeight = 10.0
                                    pane.style = "-fx-background-color:yellow"
                                    children.add(pane)//キーフレーム用のPaneを確保
                                    pp.pane = pane
                                }

                                val keyFrameIndex = v.isKeyFrame(currentFrame)
                                if(keyFrameIndex ==-1){
                                    val keyFrame = MutableProperty.KeyFrame(currentFrame, BounceInterpolator(), slider.value)
                                    v.keyFrames.add(v.getKeyFrameIndex(currentFrame) + 1,keyFrame )
                                    val circle = Circle()
                                    circle.layoutY = 5.0
                                    circle.radius = 5.0
                                    circle.fill = Paint.valueOf("BLUE")
                                    circle.layoutX = TimelineController.pixelPerFrame * currentFrame

                                    circle.setOnMouseEntered {

                                        scene.cursor = Cursor.HAND
                                        it.consume()
                                    }
                                    circle.setOnMouseMoved { it.consume() }
                                    circle.setOnMouseExited {
                                        scene.cursor = Cursor.DEFAULT
                                        it.consume()
                                    }

                                    circle.setOnMouseDragged {
                                        circle.layoutX = circle.localToParent(it.x,it.y).x
                                        it.consume()
                                    }
                                    circle.setOnMouseReleased {
                                        keyFrame.frame = (circle.layoutX / TimelineController.pixelPerFrame).toInt()
                                        v.keyFrames.sortBy { it.frame }
                                        it.consume()
                                    }
                                    circle.setOnMousePressed {
                                        //押されたキーフレームに移動
                                        timelineController.glCanvas.currentFrame = keyFrame.frame + cObject.start
                                        timelineController.caret.layoutX = timelineController.glCanvas.currentFrame * TimelineController.pixelPerFrame
                                        it.consume()
                                    }

                                    val contextMenu = ContextMenu()
                                    for(i in InterpolatorManager.interpolator){
                                        val menu = MenuItem(i.key)
                                        menu.setOnAction {
                                            keyFrame.interpolation = (i.value.newInstance() as Interpolator)
                                        }
                                        contextMenu.items.add(menu)
                                    }
                                    circle.setOnMouseClicked {
                                        if(it.button==MouseButton.SECONDARY){
                                            contextMenu.show(circle,it.screenX,it.screenY)
                                            it.consume()
                                        }
                                    }



                                    (children[i + 1] as Pane).children.add(circle)

                                    slider.style = "-fx-base:#FFFF00"
                                    println(v.keyFrames.last().value)
                                }else{
                                    v.keyFrames[keyFrameIndex].value = slider.value
                                }


                            }
                        }
                        GridPane.setMargin(slider, Insets(5.0))
                        grid.add(slider, 1, i)
                        pp.node = slider
                    }
                    is SelectableProperty -> {
                        grid.add(Label(name), 0, i)
                        val choice = ChoiceBox<String>()
                        choice.items.addAll(v.list)
                        choice.setOnAction { v.selectedIndex = choice.selectionModel.selectedIndex }
                        grid.add(choice, 1, i)


                    }
                }

            }
            grid.columnConstraints[1].hgrow = Priority.ALWAYS
            popupRoot.children.add(accordion)
        }



        if (Settings.popupEditWindow) {
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

    fun onCaretChanged(frame: Int) {
        currentFrame = frame - cObject.start
        for (ps in properties)
            for (p in ps.property) {
                val pro = p.property
                when (pro) {
                    is MutableProperty -> {
                        //フレーム移動時にプレビュー用モードオフ
                        pro.temporaryMode = false
                        (p.node as Slider).value = pro.value(currentFrame)

                        (p.node as Slider).style = "-fx-base:" + when {
                            pro.keyFrames.size == 0 -> "#323232"
                            pro.isKeyFrame(currentFrame)!=-1 -> "#FFFF00"
                            else -> "#9B5A00"
                        }
                    }
                }
            }
    }

    fun onScaleChanged() {
        layoutX = cObject.start * TimelineController.pixelPerFrame
        prefWidth = cObject.end * TimelineController.pixelPerFrame - layoutX

        for(ps in properties)
            for(p in ps.property){
                val pro = p.property
                val pane = p.pane
                if(pane!=null && pro is MutableProperty){
                    for((i,v) in pane.children.withIndex()){
                        v.layoutX = pro.keyFrames[i].frame * TimelineController.pixelPerFrame
                    }
                }
            }

    }

}