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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
        colors = if(isTopCard) CardDefaults.cardColors() else CardDefaults.cardColors(containerColor = Color.LightGray ),
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoCardStack(context: Context, photoUris: List<ImagesDataManager.ImageData>, onDelete: (Uri) -> Unit) {
    val topCard = remember { mutableIntStateOf(photoUris.lastIndex) }
    val offsetX = remember(topCard.intValue) { Animatable(0f) }

    if(topCard.intValue == -1) {
        showCardsFinishUI(context)
        return
    }
    val isFirstVisit = rememberSaveable { mutableStateOf(true) }

    val onSwipedLeft = { uri: Uri ->
        // Handle left swipe
        // delete the photo from the memory
        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
        onDelete(uri)
        println("Swiped Left: $uri")
        topCard.intValue -= 1

    }
    val onSwipedRight = { uri: Uri ->
        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()


        println("Swiped Right: $uri")
        topCard.intValue -= 1
        // Handle right swipe
        // Show next photo
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            color = when {
                offsetX.value > 0 -> Color.Green.copy((abs(offsetX.value) / 1000f))
                offsetX.value < 0 -> Color.Red.copy(alpha = (abs(offsetX.value) / 1000f))
                else -> Color.Transparent
            }
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(modifier = Modifier
                .weight(1f)
            ) {
                photoUris.forEachIndexed { index, imgData ->
                    val uri = imgData.uri
                    if (index <= topCard.intValue) {
                        DraggableCard(
                            photoUri = uri,
                            onSwipedLeft = { onSwipedLeft(uri) },
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
                        onSwipedLeft(photoUris[topCard.intValue].uri)
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
                Box(modifier = Modifier.weight(1f) ){
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
fun FileInfoCard(name: String, size: String, content1: @Composable () -> Unit = {}, content2: @Composable () -> Unit = {}){
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
            Column( horizontalAlignment = Alignment.CenterHorizontally

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
fun showCardsFinishUI(activity: Context) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(30.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){

        val openURL = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the returned result here if needed
        }
        val tooltipState = PlainTooltipState()
        LaunchedEffect(tooltipState) {
            tooltipState.show()
        }

        Box(
            modifier= Modifier
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
        ){
            PlainTooltipBox(
                tooltip = {Text("Click to view my GitHub profile")},
                tooltipState = tooltipState
            ){
                Icon(
                    Icons.Filled.Check,
                    modifier = Modifier.size(60.dp),
                    contentDescription = "Delete this Image",
                    tint = Color.White
                )
            }

        }
        Text(
            text = "Well done, you have seen all the images!",
            style = MaterialTheme.typography.displaySmall,
            color = Color.Gray,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CircularButtons(imageVector: ImageVector, contentDescription: String, color: Color, onClick: () -> Unit){
    Box(
        modifier= Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ){
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
    Column( Modifier.padding(8.dp),horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White)
    }
}