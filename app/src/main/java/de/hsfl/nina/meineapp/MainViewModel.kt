package de.hsfl.nina.meineapp
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import kotlin.math.floor
import kotlin.math.roundToInt

class MainViewModel() : ViewModel() {

    private val name: MutableLiveData<String> = MutableLiveData("Hier Namen eingeben")
    private val highscoreList = MutableLiveData(emptyList<Highscore>().toMutableList())
    private val startTime: MutableLiveData<Long> = MutableLiveData(0)
    private val elapsedTime: MutableLiveData<Long> = MutableLiveData(0)
    private val time: MutableLiveData<String> = MutableLiveData("0:0:0.0")
    private var started: MutableLiveData<Boolean> = MutableLiveData(false)
    private var done: MutableLiveData<Boolean> = MutableLiveData(false)
    private var timerJob: Job? = null
    private val xPos: MutableLiveData<Float> = MutableLiveData(0f)
    private val yPos: MutableLiveData<Float> = MutableLiveData(0f)
    private val _currentLocation: MutableLiveData<Location> = MutableLiveData()
    val currentLocation: MutableLiveData<Location> get() = _currentLocation
    private val mapPointsList = MutableLiveData<MutableList<MapPoint>>(emptyList<MapPoint>().toMutableList())
    private var bestPlayer: MutableLiveData<String> = MutableLiveData("Noch kein Spiel bisher")
    private val showToastEvent: MutableLiveData<String> = MutableLiveData()
    private var strecke: MutableLiveData<Float> = MutableLiveData(0f)
    private var distance: MutableLiveData<String> = MutableLiveData("Noch kein Spiel bisher")


    fun getName(): MutableLiveData<String> = name
    fun getDistance(): MutableLiveData<String> = distance
    fun getStrecke():MutableLiveData<Float> = strecke
    fun getBestPlayer():MutableLiveData<String> = bestPlayer
    fun getHighscoreList(): MutableLiveData<MutableList<Highscore>> = highscoreList
    fun getTime(): MutableLiveData<String> = time
    fun getStarted() : MutableLiveData<Boolean> = started
    fun getDone() : MutableLiveData<Boolean> = done
    fun getXPos(): MutableLiveData<Float> = xPos
    fun getYPos(): MutableLiveData<Float> = yPos
    fun getMapPointsList(): LiveData<MutableList<MapPoint>> = mapPointsList
    fun getShowToastEvent(): LiveData<String> = showToastEvent
    fun setMapPointsList(liste: MutableList<MapPoint>){
        mapPointsList.value = liste
    }
    fun setHighscoreList(liste: MutableList<Highscore>){
        highscoreList.value = liste
        bestPlayer.value = highscoreList.value!!.get(0).name
    }

    fun addMapPoint() {
        val mapPoint =
            _currentLocation.value?.let { MapPoint(it.latitude, _currentLocation.value!!.longitude, "nicht besucht") }
        val currentList = mapPointsList.value ?: mutableListOf()
        if (mapPoint != null) {
            currentList.add(mapPoint)
        }
        mapPointsList.value = currentList
    }

    fun reset(){
        Log.d("MVM", " Resetten")
        strecke.value = 0f
        started.value = false
        highscoreList.value = emptyList<Highscore>().toMutableList()
        bestPlayer.value = "Noch kein Spiel bisher"
        name.value = "Hier Namen eingeben"
        mapPointsList.value = emptyList<MapPoint>().toMutableList()
    }

    fun getProgressPercent():Int{
     return howManyLeft()*100 / (mapPointsList.value?.size ?:1)
    }

    fun howManyLeft():Int{
        var count = 0
        for (mapPoint in mapPointsList.value?: listOf()){
            if (mapPoint.state == "nicht besucht"){
                count++
            }
        }

        return count
    }

    fun deletePoint(i:Int){
        val newList = mapPointsList.value
        if (newList != null) {
            newList.removeAt(i)
        }
        mapPointsList.value = newList?: emptyList<MapPoint>().toMutableList()
    }

    fun updateHighscore() {
        var list = highscoreList.value ?: emptyList<Highscore>().toMutableList()
        val newScore:Highscore = Highscore(elapsedTime.value?:0,name.value?:"",distance.value?:"")

        if (newScore != null) {
            list.add(newScore)
            list.sortBy { it.time }
        }

        bestPlayer.value = highscoreList.value?.get(0)?.name
        highscoreList.postValue(list)

        Log.d("MVM", "Liste: ${highscoreList.value}")
    }

    fun getNameArray() : Array<String>{
        val array :MutableList<String> = emptyList<String>().toMutableList()
        for (i in 0 until highscoreList.value?.size!!){
            highscoreList.value?.get(i)?.let { array.add(it.name) }
        }

        return array.toTypedArray()
    }


    fun setLocation(location: Location) {
        val lastLoc = _currentLocation.value
        if (lastLoc != null && started.value == true) {
            strecke.value = strecke.value?.plus(lastLoc.distanceTo(location))
            distance.value = strecke.value?.roundToInt().toString() + " m"
        }

        _currentLocation.value = location
        val (mapX, mapY) = calculateMapCoordinates(location.latitude, location.longitude)
        xPos.value = mapX
        yPos.value = mapY
        if (started.value == true){
            for (i in 0 until (mapPointsList.value?.size ?: 0)) {
                var mapPoint = mapPointsList.value?.get(i)
                val mapPointLocation = Location("").apply {
                    if (mapPoint != null) {
                        latitude = mapPoint.latitude
                        longitude = mapPoint.longitude
                    }
                }
                if (mapPointLocation.distanceTo(location) < 5) {
                    if(allBeforeFound(i)){
                        showToastEvent.value = ""
                        if (mapPoint != null) {
                            mapPoint.state = "besucht"
                            val updatedList = mapPointsList.value?.toMutableList() ?: mutableListOf()
                            updatedList[i] = mapPoint
                            mapPointsList.value = updatedList
                            break
                        }
                    } else {
                        if(showToastEvent.value != "Dir fehlt noch ein vorheriger Punkt!"){
                            showToastEvent.value = "Dir fehlt noch ein vorheriger Punkt!"
                        }

                    }
                }
            }
            if(!somethingLeft()){
                stopGame()
            }
        }

    }

    fun allBeforeFound(index: Int): Boolean {
        var fine = true
        for (i in 0 until index) {
            if (mapPointsList.value?.get(i)?.state == "nicht besucht") {
                fine = false
                break
            }
        }
        return fine
    }

    fun somethingLeft(): Boolean {

        var somethingLeft = false
        for (mapPoint in mapPointsList.value?: listOf()){
            if (mapPoint.state == "nicht besucht"){
                somethingLeft = true
            }
        }

        return somethingLeft
    }

    fun startGame() {
        strecke.value = 0f
        resetList()
        started.value = true
        done.value = false
        startTime.value = currentTimeMillis()
        elapsedTime.value = 0

        timerJob = viewModelScope.launch {
            while (started.value == true) {
                updateElapsedTime()
                delay(1)
            }
        }
    }

    fun resetList(){
        val updatedList = mapPointsList.value?.toMutableList() ?: mutableListOf()
        for (i in 0 until (mapPointsList.value?.size ?: 0)){
            var mapPoint = mapPointsList.value?.get(i)
            if (mapPoint != null) {
                mapPoint.state = "nicht besucht"
            }
            if (mapPoint != null) {
                updatedList[i] = mapPoint
            }
        }
        mapPointsList.value = updatedList
    }

    fun stopGame() {
        if (started.value == true) {
            timerJob?.cancel()
            time.value = timeToString(currentTimeMillis() - startTime.value!!)
            updateHighscore()
            started.value = false
            done.value = true
        }
    }

    fun killGame(){
        if (started.value == true) {
            timerJob?.cancel()
            time.value = timeToString(currentTimeMillis() - startTime.value!!)
            started.value = false
        }
    }

    private suspend fun updateElapsedTime() {
        val currentTime = System.currentTimeMillis()
        elapsedTime.value = currentTime - startTime.value!!
        time.value = timeToString(elapsedTime.value!!)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private fun timeToString(timeInMillis: Long): String {
        var time = timeInMillis.toDouble()
        val millis = (time % 1000).toInt()
        time = floor(time / 1000.0)
        val seconds = (time % 60).toInt()
        time = floor(time / 60.0)
        val minutes = (time % 60).toInt()
        val hours = (time / 60.0).toInt()

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
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

    fun addPointWithXY(mapX: Float, mapY: Float){
        val mapPoint = MapPoint(calculateCoordinatesFromMap(mapX, mapY).first, calculateCoordinatesFromMap(mapX, mapY).second, "nicht besucht")
        val currentList = mapPointsList.value ?: mutableListOf()
        if (mapPoint != null) {
            currentList.add(mapPoint)
        }
        mapPointsList.value = currentList
    }

    fun calculateCoordinatesFromMap(mapX: Float, mapY: Float): Pair<Double, Double> {
        val tlLatitude = 54.778514
        val tlLongitude = 9.442749
        val brLatitude = 54.769009
        val brLongitude = 9.464722

        val posLongitude = tlLongitude + mapX * (brLongitude - tlLongitude)
        val posLatitude = tlLatitude + mapY * (brLatitude - tlLatitude)

        return Pair(posLatitude, posLongitude)
    }


}
