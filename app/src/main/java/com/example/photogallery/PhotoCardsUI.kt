package com.example.photogallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * @param photoUri: Uri of the photo to be displayed
 * @param onSwipedLeft: Callback when the card is swiped left
 * @param onSwipedRight: Callback when the card is swiped right
 * @param isTopCard: Boolean to check if the card is the top card
 * @param offsetXForBackground: Animatable<Float, AnimationVector1D> to animate the background color
 */
@Composable
fun DraggableCard(
    photoUri: Uri,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
    isTopCard: Boolean,
    offsetXForBackground: Animatable<Float, AnimationVector1D>
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = offsetX.value / 20
    val scope = rememberCoroutineScope()
    // ...
    val isFirstVisit = rememberSaveable { mutableStateOf(true) }

    if (isFirstVisit.value) {
        // ...
        LaunchedEffect(isFirstVisit.value) {
            // Delay to give the user some time to read the instructions
            while (isFirstVisit.value && isTopCard) {
                val targetOffset = 150f
                //delay(2000)
                // Animate offsetX to simulate a swipe to the right
                offsetX.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(durationMillis = 500)
                )

                // Animate offsetX back to 0 to return the card to its original position
                offsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500)
                )
                offsetX.animateTo(
                    targetValue = -targetOffset,
                    animationSpec = tween(durationMillis = 500)
                )
                offsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500)
                )
            }
        }
    }
    LaunchedEffect(offsetX.value == 0f) {

        offsetXForBackground.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300)
        )

    }

    Card(
        colors = if (isTopCard) CardDefaults.cardColors() else CardDefaults.cardColors(
            containerColor = Color.LightGray
        ),
        modifier = Modifier
            .alpha(if (isTopCard && abs(offsetX.value) > 60) 1 - abs(offsetX.value) / 3000f else 1f)
            .padding(16.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotation
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { deltaX ->
                    scope.launch {
                        offsetX.snapTo(offsetX.value + deltaX)
                        offsetXForBackground.snapTo(offsetXForBackground.value + deltaX)
                    }
                },
                onDragStopped = {
                    when {
                        offsetX.value > 200f -> {
                            // Swiped right
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 1000f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                            onSwipedRight()
                        }

                        offsetX.value < -200f -> {
                            // Swiped left
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = -1000f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                            onSwipedLeft()
                        }

                        else -> {
                            // Return to the original position
                            scope.launch {
                                offsetX.animateTo(0f, tween(300))
                            }
                        }
                    }
                }
            )
    ) {
        // Your card content here
        Image(
            painter = rememberAsyncImagePainter(photoUri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

    }
}

/**
 * This composable function displays a list of photos in a stack of cards.
 * The top card can be swiped left or right to delete or save the photo.
 * The top card is draggable horizontally.
 * The background color changes to red or green based on the direction of the swipe.
 * The top card is draggable horizontally.
 * @param context The context of the activity
 * @param photoUris The list of photo URIs, and image size and unit
 * @param onDelete The callback function to delete the photo
 */
@Composable
fun PhotoCardStack(
    context: Context,
    photoUris: List<ImagesDataManager.ImageData>,
    reload: MutableState<Int>,
    onDelete: (Uri) -> Boolean
) {
    val topCard = remember(photoUris) { mutableIntStateOf(photoUris.lastIndex) }
    val offsetX = remember(topCard.intValue) { Animatable(0f) }

    if (topCard.intValue == -1) {
        if (ImagesDataManager.imagesSeen % 30 == 0)
            showCardsFinishUI(context, reload)
        else reload.value += 1
        return
    }
    val isFirstVisit = rememberSaveable { mutableStateOf(reload.value == 0) }

    val onSwipedLeft = { imgData: ImagesDataManager.ImageData ->
        // Handle left swipe
        // delete the photo from the memory
        val uri = imgData.uri

        val deletedSuccessfully = onDelete(uri)
        println("Swiped Left: $uri")
        if (deletedSuccessfully) {
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            ImagesDataManager.storageCleared = convertToMB(imgData.size, imgData.unit)
            topCard.intValue -= 1
            ImagesDataManager.imagesSeen += 1
            ImagesDataManager.topCard = topCard.intValue

        } else {
            try {
                context.contentResolver.delete(uri, null, null)
            } catch (securityException: SecurityException) {

            }
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            ImagesDataManager.storageCleared = convertToMB(imgData.size, imgData.unit)
            topCard.intValue -= 1
            ImagesDataManager.imagesSeen += 1
            ImagesDataManager.topCard = topCard.intValue

        }

    }
    val onSwipedRight = { uri: Uri ->
        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()


        println("Swiped Right: $uri")
        topCard.intValue -= 1
        ImagesDataManager.imagesSeen += 1
        ImagesDataManager.topCard = topCard.intValue
        // Handle right swipe
        // Show next photo
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = when {
                    offsetX.value > 0 -> Color.Green.copy(min(1.0f, (abs(offsetX.value) / 1200f)))
                    offsetX.value < 0 -> Color.Red.copy(
                        alpha = min(
                            1.0f,
                            (abs(offsetX.value) / 1200f)
                        )
                    )

                    else -> Color.Transparent
                }
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = (ImagesDataManager.imagesSeen.toFloat() % 30) / 30,
                color = Color.Blue,
                // backgroundColor = Color.LightGray,
                //  strokeWidth = 4.dp,
                modifier = Modifier
                    .width(150.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                val photoUrisChunks = photoUris.chunked(4)
                val chunkIndex = remember(topCard.intValue) {
                    mutableIntStateOf(mapToChunkIndexAndItemIndex(topCard.intValue, 4).first)
                }
                val itemIndex = remember(chunkIndex.intValue) { mutableIntStateOf(0) }

//                photoUrisChunks[chunkIndex.value].forEachIndexed { index, imgData ->
//                    if (index == itemIndex.value) {
//                        DraggableCard(
//                            photoUri = imgData.uri,
//                            onSwipedLeft = { onSwipedLeft(imgData) },
//                            onSwipedRight = { onSwipedRight(imgData.uri) },
//                            isTopCard = mapToTotalIndex( chunkIndex.intValue,index,4) == topCard.intValue,
//                            offsetXForBackground = offsetX
//                        )
//                    }
//                }


                photoUris.forEachIndexed { index, imgData ->
                    val uri = imgData.uri
                    if (index <= topCard.intValue) {
                        DraggableCard(
                            photoUri = uri,
                            onSwipedLeft = { onSwipedLeft(imgData) },
                            onSwipedRight = { onSwipedRight(uri) },
                            isTopCard = index == topCard.intValue,
                            offsetXForBackground = offsetX
                        )
                    }
                }
            }
            FileInfoCard(
                name = photoUris[topCard.intValue].uri.toString().substringAfterLast('/'),
                size = photoUris[topCard.intValue].size + " " + photoUris[topCard.intValue].unit,
                content1 = {
                    CircularButtons(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Delete this image",
                        color = Color.Red
                    ) {
                        onSwipedLeft(photoUris[topCard.intValue])
                    }
                },
                content2 = {
                    CircularButtons(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Okay, next image",
                        color = Color.Green
                    ) {
                        onSwipedRight(photoUris[topCard.intValue].uri)
                    }
                }
            )
        }
    }
    if (isFirstVisit.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { isFirstVisit.value = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.matchParentSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                InstructionRow(
                    icon = { /*TODO*/ },
                    text = "This is a photo gallery app where you can discard images that you don't want to keep while saving the ones you like by swiping left or right."
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        InstructionRow(
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_swipe_left),
                                    contentDescription = "swipe left icon",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )
                            },
                            text = "Swipe left to delete the image from storage"
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        InstructionRow(
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_swipe_right),
                                    contentDescription = "swipe right con",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )
                            },
                            text = "Swipe right to retain and show next image"
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        InstructionRow(
                            icon = {

                            },
                            text = "You can also use this Delete Button"
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        InstructionRow(
                            icon = {

                            },
                            text = "You can use this Green Button to move to next image"
                        )
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            // Delay to give the user some time to read the instructions
            delay(2000)

            // Animate offsetX to simulate a swipe to the right
            offsetX.animateTo(
                targetValue = 200f,
                animationSpec = tween(durationMillis = 500)
            )

            // Animate offsetX back to 0 to return the card to its original position
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500)
            )
        }
    }
}

@Composable
fun FileInfoCard(
    name: String,
    size: String,
    content1: @Composable () -> Unit = {},
    content2: @Composable () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content1()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    modifier = Modifier.widthIn(max = 200.dp),
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = size,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            content2()
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun showCardsFinishUI(activity: Context, reload: MutableState<Int> = mutableStateOf(0)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val openURL =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                // Handle the returned result here if needed
            }
        val tooltipState = PlainTooltipState()
        LaunchedEffect(tooltipState) {
            tooltipState.show()
        }

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.Green)
                .clickable {
                    val openURLIntent = Intent(Intent.ACTION_VIEW)
                    openURLIntent.data = Uri.parse("https://github.com/Anubhvv")
                    openURL.launch(openURLIntent)
                    (activity as Activity).finish()
                },
            contentAlignment = Alignment.Center
        ) {
            PlainTooltipBox(
                tooltip = { Text("Click to view my GitHub profile") },
                tooltipState = tooltipState
            ) {
                Icon(
                    Icons.Filled.Check,
                    modifier = Modifier.size(60.dp),
                    contentDescription = "Check icon",
                    tint = Color.White
                )
            }

        }
        Text(
            text = "Well done, you have reviewed so many images!",
            style = MaterialTheme.typography.displaySmall,
            color = Color.Gray,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
                .background(MaterialTheme.colorScheme.background),
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.wrapContentHeight(),
                    text = "Total Storage Saved",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .background(Color.White)
                        .fillMaxWidth()
                )
                Text(
                    modifier = Modifier.wrapContentHeight(),
                    text = String.format("%.1f", ImagesDataManager.storageCleared) + " " +
                            "MB",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.Blue)
                .clickable {
                    reload.value += 1
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Refresh,
                modifier = Modifier.size(60.dp),
                contentDescription = "Reload icon",
                tint = Color.White
            )
        }
    }
}

@Composable
fun CircularButtons(
    imageVector: ImageVector,
    contentDescription: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector,
            modifier = Modifier.size(38.dp),
            contentDescription = contentDescription,
            tint = Color.White
        )


    }
}

@Composable
fun InstructionRow(icon: @Composable () -> Unit, text: String) {
    Column(
        Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White)
    }
}

/**
 * Function to map the total index to the chunk index and item index
 * @param totalIndex: Int topCard.intValue
 * @param chunkSize: Int
 * @return Pair<Int, Int> chunkIndex and itemIndex
 */
fun mapToChunkIndexAndItemIndex(totalIndex: Int, chunkSize: Int): Pair<Int, Int> {
    val chunkIndex = totalIndex / chunkSize
    val itemIndex = totalIndex % chunkSize
    return Pair(chunkIndex, itemIndex)
}

fun mapToTotalIndex(chunkIndex: Int, itemIndex: Int, chunkSize: Int): Int {
    return chunkIndex * chunkSize + itemIndex
}

fun convertToMB(size: String, unit: String): Float {
    return when (unit) {
        "MB" -> size.toFloat()
        "KB" -> size.toFloat() / 1024
        else -> 0f
    }
}