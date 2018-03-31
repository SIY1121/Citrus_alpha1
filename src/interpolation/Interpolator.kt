package interpolation

/**
 * キーフレームの補完をする抽象クラス
 */
abstract class Interpolator {
    abstract fun getInterpolation(input : Double):Double
}