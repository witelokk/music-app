package com.witelokk.musicapp.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.data.PlayerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSheetScaffold(
    navController: NavController,
    playerState: PlayerState,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        rememberStandardBottomSheetState(skipHiddenState = false, confirmValueChange = { value -> value != SheetValue.Hidden })
    ),
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(scaffoldState = scaffoldState, sheetPeekHeight = 150.dp, sheetContent = {
        SheetContent(navController, scaffoldState.bottomSheetState, playerState, scaffoldState)
    }, modifier = modifier.fillMaxHeight(), topBar = topBar) { innerPadding ->
        content(innerPadding)
    }

    BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetContent(
    navController: NavController,
    sheetState: SheetState,
    playerState: PlayerState,
    scaffoldState: BottomSheetScaffoldState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxWidth()
            .height(810.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) {
                if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val density = LocalDensity.current
        AnimatedVisibility(visible = scaffoldState.bottomSheetState.targetValue != SheetValue.Expanded,
            enter = slideInVertically { with(density) { -40.dp.roundToPx() } } + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()) {

            Column {
                SmallPlayer(playerState, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(38.dp))
            }
        }

        Player(navController, sheetState, playerState)
    }
}

