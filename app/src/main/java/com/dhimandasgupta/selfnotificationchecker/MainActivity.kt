package com.dhimandasgupta.selfnotificationchecker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import com.dhimandasgupta.selfnotificationchecker.ui.theme.SelfNotificationCheckerTheme
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SelfNotificationCheckerTheme { Body() }
        }
    }
}

@Preview
@Composable
fun CheckPreview() {
    SelfNotificationCheckerTheme { Body() }
}

/**
 * Fetching the notification data is pull based - So not ideal
 * */
@Composable
internal fun Body() {
    val context = LocalContext.current
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val scope = rememberCoroutineScope()

    val activeNotifications = remember { mutableStateOf(notificationManager.activeNotifications) }
    val notificationCount = remember { mutableStateOf(NotificationCount()) }

    DisposableEffect(
        key1 = "active_notifications",
        effect = {
            scope.launch {
                while (isActive) {
                    delay(1000)
                    activeNotifications.value = notificationManager.activeNotifications
                    notificationCount.value =
                        notificationManager.activeNotifications.getBadgeNumber()
                }
            }

            onDispose { scope.cancel(null) }
        }
    )


    Scaffold(
        content = { CurrentNotifications(activeNotifications.value) },
        bottomBar = {
            RowWithNotificationButtons(
                context = context,
                notificationCount = notificationCount.value
            )
        }
    )
}

@Composable
fun CurrentNotifications(
    activeNotifications: Array<StatusBarNotification>
) {
    when (activeNotifications.size) {
        0 -> EmptyNotification()
        else -> ActiveNotifications(activeNotifications)
    }
}

@Composable
fun EmptyNotification() {
    Text(
        text = "No notifications from the app",
        style = typography.h3,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 32.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ActiveNotifications(
    activeNotifications: Array<StatusBarNotification>
) {
    LazyColumn {
        items(activeNotifications) {
            val title = it.notification.extras.getString("android.title") ?: "No Title found"
            val text = it.notification.extras.getString("android.text") ?: "No Text found"
            val subText = it.notification.extras.getString("android.subText") ?: "No SubText found"

            ActiveNotification(id = "${it.id}", title = title, text = text, subText = subText)
        }
    }
}

@Composable
internal fun ActiveNotification(
    id: String,
    title: String,
    text: String,
    subText: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        backgroundColor = colors.background.copy(alpha = 0.4f),
        shape = RoundedCornerShape(size = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, colors.primary),
                contentPadding = PaddingValues(0.dp),  //avoid the little icon
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
            ) {
                Text(text = title, style = typography.body1)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                Text(text = id, style = typography.body2)
                Text(text = text, style = typography.subtitle1)
                Text(text = subText, style = typography.subtitle2)
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
internal fun RowWithNotificationButtons(
    context: Context,
    notificationCount: NotificationCount
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        RoundedButtonWithTextAndClick(
            badgeNumber = notificationCount.iconOne,
            imageResourceId = R.drawable.ic_baseline_filter_1_24,
            onClick = { createAndShowNotification(context, "One") }
        )

        RoundedButtonWithTextAndClick(
            badgeNumber = notificationCount.iconTwo,
            imageResourceId = R.drawable.ic_baseline_filter_2_24,
            onClick = { createAndShowNotification(context, "Two") }
        )

        RoundedButtonWithTextAndClick(
            badgeNumber = notificationCount.iconThree,
            imageResourceId = R.drawable.ic_baseline_filter_3_24,
            onClick = { createAndShowNotification(context, "Three") }
        )

        RoundedButtonWithTextAndClick(
            badgeNumber = notificationCount.iconFour,
            imageResourceId = R.drawable.ic_baseline_filter_4_24,
            onClick = { createAndShowNotification(context, "Four") }
        )

        RoundedButtonWithTextAndClick(
            badgeNumber = notificationCount.iconFive,
            imageResourceId = R.drawable.ic_baseline_filter_5_24,
            onClick = { createAndShowNotification(context, "Five") }
        )
    }
}

@Composable
internal fun RoundedButtonWithTextAndClick(
    badgeNumber: Int = 0,
    imageResourceId: Int,
    onClick: () -> Unit
) {
    Box {
        Button(
            onClick = onClick,
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, colors.primary),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
        ) {
            Icon(
                painter = painterResource(id = imageResourceId),
                contentDescription = null // decorative element
            )
        }

        if (badgeNumber > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            ) {
                Text(
                    text = "$badgeNumber",
                    style = typography.caption,
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}

/**
 * Creating notification with a fixed pattern
 * */
internal fun createAndShowNotification(
    context: Context,
    type: String
) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelId = "Test_Channel_Id"
    val channelName = "Test_Channel_Id"
    val channelImportance = NotificationManager.IMPORTANCE_DEFAULT
    val channelDescription = "Test notification"

    val flag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent =
        PendingIntent.getActivity(context, 0, intent, flag)

    val builder = NotificationCompat.Builder(context, channelId)
        .setContentText("This is Context Text $type")
        .setSubText("This is Context SubText $type")
        .setContentTitle(type)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            channelImportance
        ).apply {
            description = channelDescription
        }
        notificationManager.createNotificationChannel(channel)
    }

    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}

/**
 * Data class to hold the notification counts
 * */
data class NotificationCount(
    val iconOne: Int = 0,
    val iconTwo: Int = 0,
    val iconThree: Int = 0,
    val iconFour: Int = 0,
    val iconFive: Int = 0,
)

/**
 * Bad and hard coded way to extract and check the data from the notification
 *
 * If the content of the notification is dynamic and the check becomes impossible
 * */
internal fun Array<StatusBarNotification>.getBadgeNumber(): NotificationCount {
    var iconOne = 0
    var iconTwo = 0
    var iconThree = 0
    var iconFour = 0
    var iconFive = 0

    this.iterator().forEach { statusBarNotification ->
        val title =
            statusBarNotification.notification.extras.getString("android.title") ?: "No Title found"
        val text =
            statusBarNotification.notification.extras.getString("android.text") ?: "No Text found"
        val subText = statusBarNotification.notification.extras.getString("android.subText")
            ?: "No SubText found"

        when {
            (title == "One" && text == "This is Context Text One" && subText == "This is Context SubText One") -> {
                iconOne++
            }
            (title == "Two" && text == "This is Context Text Two" && subText == "This is Context SubText Two") -> {
                iconTwo++
            }
            (title == "Three" && text == "This is Context Text Three" && subText == "This is Context SubText Three") -> {
                iconThree++
            }
            (title == "Four" && text == "This is Context Text Four" && subText == "This is Context SubText Four") -> {
                iconFour++
            }
            (title == "Five" && text == "This is Context Text Five" && subText == "This is Context SubText Five") -> {
                iconFive++
            }
            else -> {
            }
        }
    }
    return NotificationCount(
        iconOne = iconOne,
        iconTwo = iconTwo,
        iconThree = iconThree,
        iconFour = iconFour,
        iconFive = iconFive
    )
}