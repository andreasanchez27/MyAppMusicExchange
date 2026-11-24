package com.example.myapplication4

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// Para los textos y text-box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp

// Para los botones y ventana
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch

// Para los graficos
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow

// Formateando los numeros
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CompareScreen(
    history: SnapshotStateList<String>,
    fontSize: Float,
    textColor: String
) {
    // Text inputs (survive screen rotation)
    val artist1Input = rememberSaveable { mutableStateOf("") }
    val artist2Input = rememberSaveable { mutableStateOf("") }

    // API results and UI states
    val artist1Result = remember { mutableStateOf<ArtistInfo?>(null) }
    val artist2Result = remember { mutableStateOf<ArtistInfo?>(null) }
    val tracks1 = remember { mutableStateOf<List<TrackInfo>>(emptyList()) }
    val tracks2 = remember { mutableStateOf<List<TrackInfo>>(emptyList()) }
    val albums1 = remember { mutableStateOf<List<AlbumInfo>>(emptyList()) }
    val albums2 = remember { mutableStateOf<List<AlbumInfo>>(emptyList()) }
    val tags1 = remember { mutableStateOf<List<TagInfo>>(emptyList()) }
    val tags2 = remember { mutableStateOf<List<TagInfo>>(emptyList()) }
    val errorMessage = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val currentTextColor = when (textColor) {
        "Blue" -> Color.Blue
        "Red" -> Color.Red
        else -> Color.Black
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Allows scrolling through all sections
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. INPUTS SECTION ---
        Text(text = "Choose an artist", fontSize = fontSize.sp, color = currentTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = artist1Input.value,
            onValueChange = { artist1Input.value = it },
            placeholder = { Text("e.g. Bad Bunny") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Choose an artist", fontSize = fontSize.sp, color = currentTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = artist2Input.value,
            onValueChange = { artist2Input.value = it },
            placeholder = { Text("e.g. Bruno Mars") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = artist1Input.value.isNotBlank() && artist2Input.value.isNotBlank() && !isLoading.value,
            onClick = {
                scope.launch {
                    isLoading.value = true
                    errorMessage.value = ""
                    try {
                        val name1 = artist1Input.value.trim()
                        val name2 = artist2Input.value.trim()

                        val res1 = RetrofitClient.api.getArtistInfo(name1)
                        val res2 = RetrofitClient.api.getArtistInfo(name2)

                        if (res1.artist != null && res2.artist != null) {
                            artist1Result.value = res1.artist
                            artist2Result.value = res2.artist

                            // Fetch Top 5 tracks for both artists
                            val top1 = RetrofitClient.api.getTopTracks(name1)
                            val top2 = RetrofitClient.api.getTopTracks(name2)
                            tracks1.value = top1.toptracks?.track ?: emptyList()
                            tracks2.value = top2.toptracks?.track ?: emptyList()

                            // Fetch Top 5 tags/genres for both artists
                            val topTags1 = RetrofitClient.api.getTopTags(name1)
                            val topTags2 = RetrofitClient.api.getTopTags(name2)
                            tags1.value = topTags1.toptags?.tag?.take(5) ?: emptyList()
                            tags2.value = topTags2.toptags?.tag?.take(5) ?: emptyList()

                            // Fetch Top albums for both artists
                            val topAlbums1 = RetrofitClient.api.getTopAlbums(name1)
                            val topAlbums2 = RetrofitClient.api.getTopAlbums(name2)
                            albums1.value = topAlbums1.topalbums?.album?.filter { it.name != "(null)" } ?: emptyList()
                            albums2.value = topAlbums2.topalbums?.album?.filter { it.name != "(null)" } ?: emptyList()

                            val summary = "${res1.artist.name} vs ${res2.artist.name}\n" +
                                    "Listeners: ${formatNumber(res1.artist.stats.listeners)} vs ${formatNumber(res2.artist.stats.listeners)}"
                            history.add(0, summary)
                        } else {
                            errorMessage.value = "One or both artists were not found."
                        }
                    } catch (e: Exception) {
                        errorMessage.value = "Connection error: ${e.message}"
                    } finally {
                        isLoading.value = false
                    }
                }
            }
        ) {
            Text(text = "Compare", fontSize = fontSize.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (isLoading.value) {
            CircularProgressIndicator()
        }

        if (errorMessage.value.isNotEmpty()) {
            Text(text = errorMessage.value, color = Color.Red, textAlign = TextAlign.Center)
        }

        // --- 2. RESULTS SECTIONS ---
        val a1 = artist1Result.value
        val a2 = artist2Result.value
        if (a1 != null && a2 != null && !isLoading.value) {
            val listeners1 = a1.stats.listeners.toLongOrNull() ?: 0L
            val listeners2 = a2.stats.listeners.toLongOrNull() ?: 0L
            val playcount1 = a1.stats.playcount.toLongOrNull() ?: 0L
            val playcount2 = a2.stats.playcount.toLongOrNull() ?: 0L

            // --- SECTION A: WINNERS (Right below Compare button) ---
            val listenersWinner = when {
                listeners1 > listeners2 -> "${a1.name} wins ${a2.name} in listeners"
                listeners2 > listeners1 -> "${a2.name} wins ${a1.name} in listeners"
                else -> "Tie in listeners!"
            }
            val playcountWinner = when {
                playcount1 > playcount2 -> "${a1.name} wins ${a2.name} in playcount"
                playcount2 > playcount1 -> "${a2.name} wins ${a1.name} in playcount"
                else -> "Tie in playcount!"
            }

            SectionTitle(title = "Winners", fontSize = fontSize, textColor = currentTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = listenersWinner,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTextColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playcountWinner,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTextColor,
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 28.dp))

            // --- SECTION B: ARTIST STATS ---
            SectionTitle(title = "Artist Stats", fontSize = fontSize, textColor = currentTextColor)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArtistResultCard(a1, fontSize, currentTextColor, Modifier.weight(1f))
                ArtistResultCard(a2, fontSize, currentTextColor, Modifier.weight(1f))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 28.dp))

            // --- SECTION C: COMPARISON OF METRICS (PROGRESS BARS) ---
            SectionTitle(title = "Comparison of metrics", fontSize = fontSize, textColor = currentTextColor)
            Spacer(modifier = Modifier.height(16.dp))
            ComparisonProgressBarsChart(
                title = "Listeners",
                artist1Name = a1.name,
                value1 = listeners1,
                artist2Name = a2.name,
                value2 = listeners2,
                textColor = currentTextColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            ComparisonProgressBarsChart(
                title = "Playcount",
                artist1Name = a1.name,
                value1 = playcount1,
                artist2Name = a2.name,
                value2 = playcount2,
                textColor = currentTextColor
            )

            // --- SECTION D: MOST FAMOUS ALBUMS ---
            if (albums1.value.isNotEmpty() && albums2.value.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 28.dp))

                SectionTitle(title = "Most Famous Albums", fontSize = fontSize, textColor = currentTextColor)
                Spacer(modifier = Modifier.height(16.dp))

                TopAlbumsVerticalChart(
                    artist1Name = a1.name,
                    albums1 = albums1.value,
                    artist2Name = a2.name,
                    albums2 = albums2.value,
                    textColor = currentTextColor
                )
            }

            // --- SECTION E: GENRE PROFILE (TOP TAGS) ---
            if (tags1.value.isNotEmpty() && tags2.value.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 28.dp))

                SectionTitle(title = "Genre Profile", fontSize = fontSize, textColor = currentTextColor)
                Spacer(modifier = Modifier.height(16.dp))

                TopTagsDonutChart(
                    artistName = a1.name,
                    tags = tags1.value,
                    accentColor = Color(0xFF6200EE),
                    textColor = currentTextColor
                )
                Spacer(modifier = Modifier.height(20.dp))
                TopTagsDonutChart(
                    artistName = a2.name,
                    tags = tags2.value,
                    accentColor = Color(0xFF00897B),
                    textColor = currentTextColor
                )
            }
        }
    }
}

// funcion para los titulos
@Composable
fun SectionTitle(
    title: String,
    fontSize: Float,
    textColor: Color
) {
    Text(
        text = title,
        fontSize = (fontSize + 4).sp,
        fontWeight = FontWeight.ExtraBold,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// funcion para los ganadores
@Composable
fun ArtistResultCard(
    artist: ArtistInfo,
    fontSize: Float,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = artist.name,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Listeners:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = formatNumber(artist.stats.listeners), fontSize = 14.sp, color = textColor)

        Spacer(modifier = Modifier.height(6.dp))

        Text(text = "Playcount:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = formatNumber(artist.stats.playcount), fontSize = 14.sp, color = textColor)
    }
}

@Composable
fun ComparisonProgressBarsChart(
    title: String,
    artist1Name: String,
    value1: Long,
    artist2Name: String,
    value2: Long,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val safe1 = value1.coerceAtLeast(0L)
    val safe2 = value2.coerceAtLeast(0L)
    val total = safe1 + safe2

    val percent1 = if (total > 0L) ((safe1 * 100L) / total).toInt().coerceIn(0, 100) else 50
    val percent2 = 100 - percent1

    val progress1 = percent1 / 100f
    val progress2 = percent2 / 100f

    val color1 = Color(0xFFD50000)
    val color2 = Color(0xFF0D47A1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Artist 1 row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = artist1Name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatNumber(safe1.toString())} ($percent1%)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color1
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress1 },
            color = color1,
            trackColor = color1.copy(alpha = 0.18f),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Artist 2 row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = artist2Name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatNumber(safe2.toString())} ($percent2%)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color2
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress2 },
            color = color2,
            trackColor = color2.copy(alpha = 0.18f),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        )
    }
}

// grafico de albums en vertical
@Composable
fun TopAlbumsVerticalChart(
    artist1Name: String,
    albums1: List<AlbumInfo>,
    artist2Name: String,
    albums2: List<AlbumInfo>,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val count = minOf(albums1.size, albums2.size, 5)
    if (count == 0) return

    val list1 = albums1.take(count)
    val list2 = albums2.take(count)

    val maxPlays = (list1 + list2)
        .maxOfOrNull { it.playcount.toLongOrNull() ?: 0L }
        ?.coerceAtLeast(1L) ?: 1L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Color legend for both artists
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6200EE))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = artist1Name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00897B))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = artist2Name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vertical bars container (160.dp max height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            for (i in 0 until count) {
                val plays1 = list1[i].playcount.toLongOrNull() ?: 0L
                val plays2 = list2[i].playcount.toLongOrNull() ?: 0L

                val frac1 = (plays1.toFloat() / maxPlays.toFloat()).coerceIn(0.05f, 1f)
                val frac2 = (plays2.toFloat() / maxPlays.toFloat()).coerceIn(0.05f, 1f)

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(frac1)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(Color(0xFF6200EE))
                    )
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(frac2)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(Color(0xFF00897B))
                    )
                }
            }
        }

        // Baseline (X-axis line)
        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // X-axis labels (#1 to #5)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0 until count) {
                Text(
                    text = "#${i + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ranking #1 to #5 for Artist 1
        Text(
            text = artist1Name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        list1.take(5).forEachIndexed { index, album ->
            Text(
                text = "#${index + 1} — ${album.name} (${formatNumber(album.playcount)})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ranking #1 to #5 for Artist 2
        Text(
            text = artist2Name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00897B),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        list2.take(5).forEachIndexed { index, album ->
            Text(
                text = "#${index + 1} — ${album.name} (${formatNumber(album.playcount)})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
            )
        }
    }
}

// Para formatear los numeros
fun formatNumber(value: String): String {
    val number = value.toLongOrNull() ?: return value
    return NumberFormat.getInstance(Locale("es", "ES")).format(number)
}

// grafico donut para los generos
@Composable
fun TopTagsDonutChart(
    artistName: String,
    tags: List<TagInfo>,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val sliceColors = listOf(
        accentColor,
        Color(0xFFFF7043),
        Color(0xFF42A5F5),
        Color(0xFFFFCA28),
        Color(0xFFAB47BC)
    )
    val totalScore = tags.sumOf { it.count.coerceAtLeast(0) }.coerceAtLeast(1)
    val rawPercentages = tags.map { ((it.count.coerceAtLeast(0) * 100f) / totalScore).toInt() }
    val diff = 100 - rawPercentages.sum()
    val maxIndex = rawPercentages.indices.maxByOrNull { rawPercentages[it] } ?: 0
    val normalizedPercentages = rawPercentages.mapIndexed { idx, value ->
        if (idx == maxIndex) (value + diff).coerceAtLeast(0) else value.coerceAtLeast(0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Top Genres - $artistName",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = textColor
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .padding(10.dp)
            ) {
                var startAngle = -90f
                tags.forEachIndexed { index, tag ->
                    val weight = tag.count.coerceAtLeast(0).toFloat() / totalScore.toFloat()
                    val sweepAngle = weight * 360f
                    drawArc(
                        color = sliceColors[index % sliceColors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24f)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEachIndexed { index, tag ->
                    val sharePercent = normalizedPercentages.getOrElse(index) { 0 }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(sliceColors[index % sliceColors.size])
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${tag.name} ($sharePercent%)",
                            fontSize = 13.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    history: SnapshotStateList<String>,
    fontSize: Float,
    textColor: String
) {
    Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Text(
            text = stringResource(id = R.string.ecra02),
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
    }
}

@Composable
fun SettingScreen(
    fontSize: MutableState<Float>,
    textColor: MutableState<String>
) {
    Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Text(
            text = stringResource(id = R.string.ecra03),
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
    }
}

@Composable
fun ListAllScreen() {
    Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Text(
            text = stringResource(id = R.string.ecra04),
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
    }
}