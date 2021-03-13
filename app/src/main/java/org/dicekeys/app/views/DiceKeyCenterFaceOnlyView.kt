package org.dicekeys.app.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import androidx.core.content.ContextCompat
import org.dicekeys.app.R
import org.dicekeys.dicekey.Face

class DiceKeyCenterFaceOnlyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var centerFace: Face = Face(letter = 'A', digit = '1')

    var size: SizeF = SizeF(50f, 50f)

    val fractionalDistanceFromCenterToCornerEdge: Float
        get() {
            val radius: Float = 1.0f / 8.0f
            val distBeforeCurve: Float = 0.5f - radius
            val distanceToCurveEdge = radius * (Math.sin(Math.PI / 4f)).toFloat()
            return distBeforeCurve + distanceToCurveEdge
        }

    val faceMagnificationFactor: Float = 4f

    val magnifiedFaceFractionalOffset: PointF = PointF(0f, -0.7f)

    // Use a unit model (fitting the DicKey box into 1x1 square bounds) to calculate the
    // extra space (overflow) required to inlucde any part of the magnified face that exceeds
    // those boounds
    val unitModel = DiceSizeModel(1f, hasTab = true)
    val unitModelMagnifiedFaceSize: Float get() = unitModel.faceSize * faceMagnificationFactor

    val unitOverflowTop: Float
        get() = Math.max(0f,
                -(unitModel.boxCenterY + (-0.5f + magnifiedFaceFractionalOffset.y) * unitModelMagnifiedFaceSize)
        )
    val unitOverflowBottom: Float
        get() = Math.max(0f,
                (unitModel.boxCenterY + (0.5f + magnifiedFaceFractionalOffset.y) * unitModelMagnifiedFaceSize) - 1
        )
    val unitOverflowLeft: Float
        get() = Math.max(0f,
                (0 - (unitModel.centerX + (-0.5f + magnifiedFaceFractionalOffset.x) * unitModelMagnifiedFaceSize)) / unitModel.width
        )
    val unitOverflowRight: Float
        get() = Math.max(0f,
                ((unitModel.centerX + (0.5f + magnifiedFaceFractionalOffset.x) * unitModelMagnifiedFaceSize) - unitModel.width) / unitModel.width
        )

    val unitOverflowHorizontal: Float get() = unitOverflowLeft + unitOverflowRight
    val unitOverflowVertical: Float get() = unitOverflowTop + unitOverflowBottom

    // Now create a DiceKey size model that includes space for that overlap
    val diceKeySizeModel: DiceSizeModel
        get() =
            DiceSizeModel(
                    SizeF(
                            size.width / (1f + unitOverflowHorizontal),
                            size.height / (1f + unitOverflowVertical)
                    ), hasTab = true
            )

    val originalFaceSize: Float get() = diceKeySizeModel.faceSize
    val magnifiedFaceSize: Float get() = diceKeySizeModel.faceSize * faceMagnificationFactor

    val originalFaceDistanceFromCenterToCornerEdge: Float
        get() =
            originalFaceSize * fractionalDistanceFromCenterToCornerEdge

    val magnifiedFaceDistanceFromCenterToCornerEdge: Float
        get() =
            magnifiedFaceSize * fractionalDistanceFromCenterToCornerEdge

    val aspectRatio: Float
        get() =
            (unitModel.width + unitOverflowHorizontal * unitModel.width) / (unitModel.height + unitOverflowVertical)
    val totalWidth: Float get() = Math.min(size.width, diceKeySizeModel.width * (1 + unitOverflowHorizontal))
    val totalHeight: Float get() = Math.min(size.height, diceKeySizeModel.height * (1 + unitOverflowVertical))

    val offsetTop: Float get() = diceKeySizeModel.height * unitOverflowTop
    val offsetLeft: Float get() = diceKeySizeModel.width * unitOverflowLeft
    val offsetBottom: Float get() = diceKeySizeModel.height * unitOverflowBottom
    val offsetRight: Float get() = diceKeySizeModel.width * unitOverflowRight
    val offsetTopForCenteredObjects: Float get() = (offsetTop / 2) - (offsetBottom / 2)
    val offsetLeftForCenteredObjects: Float get() = (offsetLeft / 2) - (offsetRight / 2)

    val diceKeyBoxCenterX: Float get() = offsetLeft + diceKeySizeModel.centerX
    val diceKeyBoxCenterY: Float get() = offsetTop + diceKeySizeModel.boxCenterY

    val originalFaceCornerLeft: Float get() = diceKeyBoxCenterX - originalFaceDistanceFromCenterToCornerEdge
    val originalFaceCornerRight: Float get() = diceKeyBoxCenterX + originalFaceDistanceFromCenterToCornerEdge
    val originalFaceCornerTop: Float get() = diceKeyBoxCenterY - originalFaceDistanceFromCenterToCornerEdge
    val originalFaceCornerBottom: Float get() = diceKeyBoxCenterY + originalFaceDistanceFromCenterToCornerEdge
    val originalFaceCornerTopLeft: PointF get() = PointF(originalFaceCornerLeft, originalFaceCornerTop)
    val originalFaceCornerTopRight: PointF get() = PointF(originalFaceCornerRight, originalFaceCornerTop)
    val originalFaceCornerBottomLeft: PointF get() = PointF(originalFaceCornerLeft, originalFaceCornerBottom)
    val originalFaceCornerBottomRight: PointF get() = PointF(originalFaceCornerRight, originalFaceCornerBottom)

    val magnifiedFaceCenter: PointF
        get() = PointF(
                diceKeyBoxCenterX + magnifiedFaceFractionalOffset.x * magnifiedFaceSize,
                diceKeyBoxCenterY + magnifiedFaceFractionalOffset.y * magnifiedFaceSize
        )
    val magnifiedFaceCornerLeft: Float get() = magnifiedFaceCenter.x - magnifiedFaceDistanceFromCenterToCornerEdge
    val magnifiedFaceCornerRight: Float get() = magnifiedFaceCenter.x + magnifiedFaceDistanceFromCenterToCornerEdge
    val magnifiedFaceCornerTop: Float get() = magnifiedFaceCenter.y - magnifiedFaceDistanceFromCenterToCornerEdge
    val magnifiedFaceCornerBottom: Float get() = magnifiedFaceCenter.y + magnifiedFaceDistanceFromCenterToCornerEdge

    val linePaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    val diceBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val diePenPaint = Paint()
    val faceSurfacePaint = Paint()
    val borderColor = Paint()

    val centerDieDrawable: DieFace get() = DieFace(centerFace, originalFaceSize, penColor = diePenPaint.color, faceSurfaceColor = faceSurfacePaint.color)
    val magnifiedCenterDieDrawable: DieFace get() = DieFace(centerFace, magnifiedFaceSize, penColor = diePenPaint.color, faceSurfaceColor = faceSurfacePaint.color, faceBorderColor = borderColor.color)
    val dieLidShape: DieLidShape get() = DieLidShape(diceKeySizeModel.lidTabRadius, diceBoxPaint.color)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StickerSheetView)
        linePaint.color = typedArray.getColor(R.styleable.DiceKeyCenterFaceOnlyView_lineColor,
                Color.argb(255 / 2, 255, 255, 255))
        diceBoxPaint.color = typedArray.getColor(R.styleable.DiceKeyCenterFaceOnlyView_boxColor,
            ContextCompat.getColor(context, R.color.alexandrasBlueLighter))
        diePenPaint.color = typedArray.getColor(R.styleable.DiceKeyCenterFaceOnlyView_penColor, Color.BLACK)
        faceSurfacePaint.color = typedArray.getColor(R.styleable.DiceKeyCenterFaceOnlyView_faceColor, Color.WHITE)
        borderColor.color = typedArray.getColor(R.styleable.DiceKeyCenterFaceOnlyView_borderColor, Color.GRAY)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            canvas.save()
            val sizeModel = diceKeySizeModel
            val l = diceKeyBoxCenterX - sizeModel.linearSizeOfBox / 2f
            val t = diceKeyBoxCenterY - sizeModel.linearSizeOfBox / 2f
            canvas.translate(l, t)
            canvas.drawRoundRect(0f, 0f, sizeModel.linearSizeOfBox, sizeModel.linearSizeOfBox,
                    sizeModel.boxCornerRadius, sizeModel.boxCornerRadius, diceBoxPaint)

            canvas.save()
            canvas.translate(sizeModel.linearSizeOfBox / 2f - sizeModel.lidTabRadius, sizeModel.linearSizeOfBox - 1)
            dieLidShape.draw(canvas)
            canvas.restore()

            canvas.restore()

            linePaint.strokeWidth = Math.max(sizeModel.linearSizeOfBox / 75f, 2f)
            // TODO: simplify
            canvas.drawLine(originalFaceCornerLeft, originalFaceCornerTop,
                    magnifiedFaceCornerLeft, magnifiedFaceCornerTop, linePaint)
            canvas.drawLine(originalFaceCornerRight, originalFaceCornerTop,
                    magnifiedFaceCornerRight, magnifiedFaceCornerTop, linePaint)
            canvas.drawLine(originalFaceCornerLeft, originalFaceCornerBottom,
                    magnifiedFaceCornerLeft, magnifiedFaceCornerBottom, linePaint)
            canvas.drawLine(originalFaceCornerRight, originalFaceCornerBottom,
                    magnifiedFaceCornerRight, magnifiedFaceCornerBottom, linePaint)

            canvas.save()
            val offsetOriginal = originalFaceSize * 0.5f - originalFaceDistanceFromCenterToCornerEdge
            canvas.translate(originalFaceCornerLeft - offsetOriginal, originalFaceCornerTop - offsetOriginal)
            val centerDieDrawable = centerDieDrawable
            centerDieDrawable.alpha = 255 / 3
            centerDieDrawable.draw(canvas)
            canvas.restore()

            canvas.save()
            val offsetMagnified = magnifiedFaceSize * 0.5f - magnifiedFaceDistanceFromCenterToCornerEdge
            canvas.translate(magnifiedFaceCornerLeft - offsetMagnified, magnifiedFaceCornerTop - offsetMagnified)
            magnifiedCenterDieDrawable.draw(canvas)
            canvas.restore()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        var height = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        // Find the correct width/height that will respect the aspect ratio
        val newHeight = width * ASPECT_RATIO

        if(newHeight < height){
            height = newHeight.toFloat()
        }else{
            width = (height / ASPECT_RATIO).toFloat()
        }

        size = SizeF(width, height)

        val totalWidth = totalWidth.toInt()
        val totalHeight = totalHeight.toInt()

        setMeasuredDimension(
                totalWidth,
                totalHeight
        )
    }

    companion object{
        const val ASPECT_RATIO = 1.5
    }
}