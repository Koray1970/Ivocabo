package com.serko.ivocabo.remote.device.missingdevicelist

import com.serko.ivocabo.remote.membership.EventResult

data class MissingDeviceListResponse(
    val devicelist: List<Devicelist>,
    val eventResult: EventResult
)