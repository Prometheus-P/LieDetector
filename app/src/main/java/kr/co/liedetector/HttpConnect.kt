package kr.co.liedetector

import android.app.AlertDialog
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kr.co.liedetector.databinding.AnswerPageBinding
import kr.co.liedetector.fragments.ImageSendFragment.Companion.isFinished
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.net.URL
data class ImageData(val validLines:ArrayList<String>, val answer:String, val answerNumber:Int)
class HttpConnect(private val binding: AnswerPageBinding, url: String, imageUrl: String, imageFormat: String) {
    val client = OkHttpClient()
    private val full="$url?img_url=$imageUrl&img_format=$imageFormat"
    private val fullUrl= URL(full)
    var count=0
    fun sendHttp() {
        try {
            Log.d("HttpConnection", full)
            val request = Request.Builder().url(fullUrl).method("GET",null).build()
            val call = client.newCall(request)
            callEnqueue(call)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
private fun callEnqueue(call: Call){

    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            val reRequest = Request.Builder().url(fullUrl).method("GET",null).build()
            CoroutineScope(Dispatchers.IO).launch {
                count++
                if (count>2) return@launch
                client.newCall(reRequest).execute()
            }
        }
        // main thread말고 별도의 thread에서 실행해야 함.
        override fun onResponse(call: Call, response: Response) {
            CoroutineScope(Dispatchers.Main).launch {
                val str = response.body?.string()
                if (str != null) {
                    Log.d("HttpConnection",  str)
                    val imageData=parseJson(str)
                    if(imageData==null){
                        count++
                        sendHttp()
                        return@launch
                    }
                    binding.text1.text= imageData.validLines[0]
                    binding.text2.text= imageData.validLines[1]
                    binding.text3.text= imageData.validLines[2]
                    binding.text4.text= imageData.validLines[3]
                    binding.text5.text= imageData.validLines[4]
                    binding.textAnswer.text=imageData.answer
                    isFinished=true
                }
            }
        }
    })
}
    fun parseJson(respJson:String):ImageData?{
        val imageData:ImageData
        try{
            val jsonObject = JSONObject(respJson)
            val data = jsonObject.get("data")
            imageData = Gson().fromJson(data.toString(), ImageData::class.java)
            Log.d("HttpConnection",  data.toString())
            Log.d("HttpConnection",  imageData.toString())
            return imageData
        }catch(e: JSONException){
            e.printStackTrace()
            sendHttp()
        }
        return null
    }
}

//https://maple-solver.vercel.app/api/solver?img_url=https://s3.ap-northeast-2.amazonaws.com/yamujin.io/uploads/bran-d/KakaoTalk_Image_2023-05-11-04-19-57_009.jpeg&img_format=jpg
//            url:
//            https://maple-solver.vercel.app/api/solver
//
//            params:
//            img_url (String, 이미지 url)
//            img_format (String, 이미지 format