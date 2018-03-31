package interpolation

/**
 * 直線補間を行う
 */
class LinearInterpolator : Interpolator() {
    override fun getInterpolation(input: Double):Double  {
        return input
    }
}