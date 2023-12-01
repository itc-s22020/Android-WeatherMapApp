package com.example.weathermapapp

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.weathermapapp.api.WeatherApp
import com.example.weathermapapp.model.City
import com.example.weathermapapp.model.ForecastList
import com.example.weathermapapp.model.Main
import com.example.weathermapapp.model.Rain
import com.example.weathermapapp.model.Snow
import com.example.weathermapapp.model.Weather
import com.example.weathermapapp.model.WeatherModel
import com.example.weathermapapp.model.Wind
import com.example.weathermapapp.ui.theme.WeatherMapAppTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import java.lang.Math.round
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherMapAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OnMainScreen("")
                }
            }
        }
    }
}


@OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnMainScreen(inText: String) {

    //なんかいるやつ
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //都道府県選択
    var inputText by remember { mutableStateOf(inText) }
    var mapData by remember { mutableStateOf(mapOf("" to "")) }
    var dateTimeTxtArray by remember { mutableStateOf(arrayOf("")) }
    var result by remember { mutableStateOf(WeatherModel( //都道府県選択の結果
        city = City(name = ""),
        forecast = listOf(ForecastList(
            dt_txt = "",
            main = Main(
                temp = 1.0, feels_like = 1.0, grnd_level = 0, humidity = 0
            ),
            weather = listOf(Weather(
                description = "",
                icon = ""
            )),
            wind = Wind(speed = 1.0, deg = 0, gust = 1.0),
            pop = 1.0,
            snow = Snow(snow = null),
            rain = Rain(rain = null)
        ),
        )
    )) }

    //日付選択
    var dateExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var dates by remember { mutableStateOf(arrayOf("選択肢なし")) }

    //時間選択
    var timeExpanded by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("") }
    var times by remember { mutableStateOf(arrayOf("選択肢なし")) }

    //天気予報表示
    var dateTime by remember { mutableStateOf("") } //日時
    var temp by remember { mutableStateOf("") } //温度
    var feelLike by remember { mutableStateOf("") } //体感温度
    var grndLevel by remember { mutableStateOf("") } //地上の大気圧 hPa
    var humidity by remember { mutableStateOf("") } //湿度 %
    var descriptor by remember { mutableStateOf("") } //気象情報
    var iconUrl by remember { mutableStateOf("") } //アイコン
    var speed by remember { mutableStateOf("") } //風速 m/s
    var deg by remember { mutableStateOf("") } //風向 度
    var gust by remember { mutableStateOf("") } //瞬間風速 m/s
    var pop by remember { mutableStateOf("") } //降水確率 1~0
    var snow by remember { mutableStateOf("") } //降水量 3h
    var rain by remember { mutableStateOf("") } //積雪量 3h

    //フォーカスを使用してTextFieldの使用を管理
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current


    
    //Forecast更新
    fun updateForecast(index: Int) {
        val res = result.forecast[index]
        dateTime = res.dt_txt
        temp = "温度:${res.main.temp}℃"
        feelLike = "体感温度:${res.main.feels_like}℃"
        grndLevel = "大気圧:${res.main.grnd_level}hPa"
        humidity = "湿度:${res.main.humidity}%"
        descriptor = res.weather[0].description
        iconUrl = res.weather[0].icon
        speed = "風速:${res.wind.speed}m/s"
        deg = "風向:${res.wind.deg}°"
        gust = "瞬間風速:${res.wind.gust}m/s"
        pop = "降水率:${(res.pop * 100).roundToInt()}%"

        fun check(s: Rain?=null, r: Snow?=null): String? {
            val regex = Regex("rain=([0-9.]+)")
            val matchResultS = regex.find(s.toString())
            val matchResultR = regex.find(s.toString())
            return if
                    (matchResultS.toString() != "null")
                    "降水量:${matchResultS?.groupValues?.get(1)}mm" else if (matchResultR.toString() != "null") "積雪量:${(matchResultR?.groupValues?.get(1))}mm" else ""
        }

        snow = "${check(null,res.snow)}"
        rain = "${check(res.rain,null)}"
    }

    //クリックしたときForecast更新
    fun updateClick() {
        result.forecast.forEachIndexed{ i, v ->
            if (v.dt_txt == "$selectedDate $selectedTime") {
                updateForecast(i)
            }
        }
    }


    //都道府県選択
    Column(
        Modifier.padding(10.dp, 10.dp, 10.dp, 0.dp)
    ) {
        OutlinedTextField(
            value = inputText,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = TextStyle(fontSize = 40.sp),
            onValueChange = {
                inputText = it
                mapData = Dataset.filterValues { key ->
                    inputText in key
                }
            },
            label = { Text(text = "都道府県") },
            singleLine = true,
            trailingIcon = {
                Icon(
                    Icons.Sharp.LocationOn,
                    contentDescription = null,
                    tint = colorResource(
                        id = R.color.purple_700
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            //あとで書く
                            inputText = "沖縄県"
                        }

                )
            }

        )
        Column (

        )
        {
            mapData.forEach { selectionOption ->
                if (selectionOption.key != "" && selectionOption.value != "") {
                    //選択肢の表示
                    Text(text = selectionOption.key,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(5.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(5.dp)
                            )

                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable {
                                inputText = selectionOption.key
                                //フォーカスをクリアしてTextFieldの使用を終了
                                focusManager.clearFocus()
                                //Columnの選択肢初期化
                                mapData = mapOf("" to "")

                                scope.launch {
                                    val res = WeatherApp.getWeatherData(selectionOption.key)
                                    result = res

                                    //日付選択テキストフィールドのArray更新 + select
                                    var dateArray = emptyArray<String>()
                                    for (i in 0..<res.forecast.size) {
                                        dateArray += res.forecast[i].dt_txt.split(" ")[0]
                                    }
                                    dates = dateArray
                                        .distinct()
                                        .toTypedArray()
                                    selectedDate = dates[0]

                                    var setDateTextArray = emptyArray<String>()
                                    for (i in 0..<res.forecast.size) {
                                        setDateTextArray += res.forecast[i].dt_txt
                                    }
                                    dateTimeTxtArray = setDateTextArray

                                    //時間選択テキストフィールドのArray更新 + select
                                    times = getTimeArray(
                                        selectedDate,
                                        dateTimeTxtArray,
                                        dateTimeTxtArray.size
                                    )
                                    selectedTime = times[0]

                                    updateForecast(0)
                                }
                            }
                    )
                }
            }
        }

        //日付,時間選択
        Row(


        ) {

            //日付選択
            ExposedDropdownMenuBox(
                expanded = dateExpanded,
                onExpandedChange = {
                    dateExpanded = !dateExpanded
                }
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    label = { Text(text = "日付") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .width(240.dp)
                        .padding(0.dp, 0.dp, 5.dp, 0.dp)

                )

                ExposedDropdownMenu(
                    expanded = dateExpanded,
                    onDismissRequest = { dateExpanded = false }
                ) {
                    dates.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                selectedDate = item
                                dateExpanded = false
                                focusManager.clearFocus()

                                scope.launch {
                                    times = getTimeArray(selectedDate, dateTimeTxtArray, dateTimeTxtArray.size )
                                    updateClick()
                                }
                            }
                        )
                    }

                }
            }

            //時間選択
            ExposedDropdownMenuBox(
                expanded = timeExpanded,
                onExpandedChange = {
                    timeExpanded = !timeExpanded
                }
            ) {
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = {},
                    label = { Text(text = "時間") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                    modifier = Modifier.menuAnchor()

                )

                ExposedDropdownMenu(
                    expanded = timeExpanded,
                    onDismissRequest = { timeExpanded = false }
                ) {
                    times.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                selectedTime = item
                                timeExpanded = false
                                focusManager.clearFocus()
                                updateClick()
                            }
                        )
                    }
                }
            }
        }

        //天気予報表示
        ElevatedCard(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(0.dp, 10.dp, 0.dp, 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceTint
            )
        ) {
            Row {
                Image(
                    painter = rememberAsyncImagePainter("https://openweathermap.org/img/wn/$iconUrl@2x.png"),
                    contentDescription = null,
                    Modifier.size(200.dp)
                )

                Column {
                    Text(descriptor)
                    Text(snow)
                    Text(rain)
                    Text(temp)
                    Text(feelLike)
                    Text(grndLevel)
                    Text(humidity)
                    Text(speed)
                    Text(deg)
                    Text(gust)
                    Text(pop)
                }

            }
        }

    }

}
//タイピングしたのでどこか間違えてるかも
val Dataset = mapOf(
    "北海道" to "HOKKAIDO hokkaido ほっかいどう 北海道",
    "青森県" to "AOMORI aomori あおもり 青森県",
    "岩手県" to "IWATE iwate いわて 岩手県",
    "宮城県" to "MIYAGI miyagi みやぎ 宮城県",
    "秋田県" to "AKITA akita あきた 秋田県",
    "山形県" to "YAMAGATA yamagata やまがた 山形県",
    "福島県" to "HUKUSIMA hukusima ふくしま 福島県",
    "茨城県" to "IBARAKI ibaraki いばらき 茨城県",
    "栃木県" to "TOTIGI totigi とちぎ 栃木県",
    "群馬県" to "GUNNMA gunnma ぐんま 群馬県",
    "埼玉県" to "SAITAMA saitama さいたま 埼玉県",
    "千葉県" to "TIBA tiba ちば 千葉県",
    "東京都" to "TOKYO tokyo とうきょうと 東京都",
    "神奈川県" to "KANAGAWA kanagawa かながわ 神奈川県",
    "新潟県" to "NIIGATA niigats にいがた 新潟県",
    "富山県" to "TOYAMA toyama とやま 富山県",
    "石川県" to "ISIKAWA isikawa いしかわ　石川県",
    "福井県" to "HUKUI hukui ふくい 福井県",
    "山梨県" to "YAMANASI yamanasi やまなし 山梨県",
    "長野県" to "NAGANO nagano ながの 長野県",
    "岐阜県" to "GIFUKENN gifuken ぎふ 岐阜県",
    "静岡県" to "SIZUOKA sizuoka しずおか 静岡県",
    "愛知県" to "AITI aiti あいち 愛知県",
    "三重県" to "MIE mie みえ 三重県",
    "滋賀県" to "SIGA siga しが 滋賀県",
    "京都県" to "KYOTO kyoto きょうと 京都県",
    "大阪府" to "OOSAKA oosaka おおさか 大阪県",
    "兵庫県" to "HYOUGO hyougo ひょうご 兵庫県",
    "奈良県" to "NARA nara なら 奈良県",
    "和歌山県" to "WAKAYAMA wakayama わかやま 和歌山県",
    "鳥取県" to "TOTTORI tottri とっとり 鳥取県",
    "島根県" to "SIMANE simane しまね 島根県",
    "岡山県" to "OKAYAMA okayama おかやま 岡山県",
    "広島県" to "HIROSIMA hirosima ひろしま 広島県",
    "山口県" to "YAMAGUTI yamaguti やまぐち 山口県",
    "徳島県" to "TOKUSIMA tokusima とくしま 徳島県",
    "香川県" to "KAGAWA kagawa かがわ 香川県",
    "愛知県" to "AITI aiti あいち 愛知県",
    "高知県" to "KOUTI kouti こうち 高知県",
    "福島県" to "HUKUSIMA hukusima ふくしま 福島県",
    "佐賀県" to "SAGA saga さが 佐賀県",
    "長崎県" to "NAGASAKI nagasaki ながさき 長崎県",
    "熊本県" to "KUMAMOTO kumato くまもと 熊本県",
    "大分県" to "OOITA ooita おおいた 大分県",
    "宮城県" to "MIYAGI miyagi みやぎ 宮城県",
    "鹿児島県" to "KAGOSIMA kagosima かごしま 鹿児島県",
    "沖縄県" to "OKINAWA okinawa おきなわ 沖縄県"
)


suspend fun getTimeArray(
    selectedDate: String,
    dateTimeTxt: Array<String>,
    forecastSize: Int
    ): Array<String> {
    var timeArray = emptyArray<String>()
    var count = 0
    for (i in 0..<forecastSize) {
        if (dateTimeTxt[i].split(" ")[0] == selectedDate) {
            count += 1
            timeArray += dateTimeTxt[i].split(" ")[1]
        } else { if (count > 0) {break} }
    }
    return timeArray
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun OnMainScreenPreview() {
    WeatherMapAppTheme {
        OnMainScreen("test")
    }
}