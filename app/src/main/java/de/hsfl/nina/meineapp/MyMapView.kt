package de.hsfl.nina.meineapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class MyMapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    private var xPos: Float = 0.0f
    private var yPos: Float = 0.0f
    private lateinit var mapBitmap: Bitmap
    private val matrix = Matrix()
    private var mapPointsList: MutableList<MapPoint> = mutableListOf()

    fun setPointsList(list:MutableList<MapPoint>){
        mapPointsList = list
    }

    fun setXPos(x: Float) {
        xPos = x
    }

    fun setYPos(y: Float) {
        yPos = y

    }


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyMapView)
        val mapImageResId = typedArray.getResourceId(R.styleable.MyMapView_mapImage, -1)
        typedArray.recycle()

        if (mapImageResId != -1) {
            mapBitmap = BitmapFactory.decodeResource(resources, mapImageResId)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scaleX = width.toFloat() / mapBitmap.width
        val scaleY = height.toFloat() / mapBitmap.height

        val scaleFactor = minOf(scaleX, scaleY)

        matrix.setScale(scaleFactor, scaleFactor)

        matrix.postTranslate(
            (width - mapBitmap.width * scaleFactor) / 2,
            (height - mapBitmap.height * scaleFactor) / 2
        )

        mapBitmap?.let {
            canvas.drawBitmap(it, matrix, null)
        }

        drawMapPoints(canvas)
        drawCurrentPosition(canvas, xPos, yPos , Color.BLUE)
    }

    private fun drawCurrentPosition(canvas: Canvas, x: Float, y: Float, color: Int) {
        val transformedWidth = matrix.mapRadius(mapBitmap.width.toFloat())
        val transformedHeight = matrix.mapRadius(mapBitmap.height.toFloat())
        val topLeft: Pair<Float, Float> = Pair(width.toFloat() - transformedWidth, height.toFloat() - transformedHeight - (height.toFloat() - transformedHeight) / 2)

        if (color == Color.BLUE) {
            paint.color = color
            paint.style = Paint.Style.FILL
            canvas.drawCircle(
                topLeft.first + x * transformedWidth,
                topLeft.second + y * transformedHeight,
                10f,
                paint
            )
        } else {
            // Zeichne den Stab
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            canvas.drawRect(
                topLeft.first + x * transformedWidth - 5f,
                topLeft.second + y * transformedHeight - 20f,
                topLeft.first + x * transformedWidth + 5f,
                topLeft.second + y * transformedHeight + 20f,
                paint
            )

            // Zeichne die Flagge
            paint.color = color
            paint.style = Paint.Style.FILL
            canvas.drawRect(
                topLeft.first + x * transformedWidth,
                topLeft.second + y * transformedHeight - 20f,
                topLeft.first + x * transformedWidth + 20f,
                topLeft.second + y * transformedHeight,
                paint
            )
        }
    }

    fun drawMapPoints(canvas: Canvas) {
        for (i in 0 until mapPointsList.size) {
            val mapPoint = mapPointsList.get(i)
            val (pointX, pointY) = calculateMapCoordinates(mapPoint.latitude, mapPoint.longitude)
            var color = Color.GRAY
                if (mapPoint.state == "besucht") {
                    color = Color.TRANSPARENT
                } else if (i > 0){
                    if(mapPointsList.get(i-1).state == "besucht" && mapPointsList.get(i).state == "nicht besucht"){
                    color = Color.RED
                    }
                } else {
                    color = Color.RED
                }
            drawCurrentPosition(canvas, pointX, pointY, color)
        }
    }

    fun calculateMapCoordinates(posLatitude: Double, posLongitude: Double): Pair<Float, Float> {
        val tlLatitude = 54.778514
        val tlLongitude = 9.442749
        val brLatitude = 54.769009
        val brLongitude = 9.464722

        val mapX = ((posLongitude - tlLongitude) / (brLongitude - tlLongitude)).toFloat()
        val mapY = ((posLatitude - tlLatitude) / (brLatitude - tlLatitude)).toFloat()

        return Pair(mapX, mapY)
    }


}
