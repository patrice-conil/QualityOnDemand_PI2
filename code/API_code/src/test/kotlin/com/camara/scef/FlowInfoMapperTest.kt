/*
* Software Name : camara-qod-api
* Version: 0.1.0
* SPDX-FileCopyrightText: Copyright (c) 2022 Orange
* SPDX-License-Identifier: Apache-2.0
*
* This software is distributed under the Apache-2.0,
* the text of which is available at https://www.apache.org/licenses/LICENSE-2.0
* or see the "LICENCE" file for more details.
*
* Author: patrice.conil@orange.com
*/

package com.camara.scef

import com.camara.TestUtils
import com.camara.model.PortsSpec
import com.camara.model.PortsSpecRangesInner
import com.camara.model.QosProfile
import com.camara.scef.FlowInfoMapper.Companion.PROTOCOL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FlowInfoMapperTest {


    val flowInfoMapper = FlowInfoMapper()

    @Test
    fun `flowInfos should build expected IPFilterRule from sessionInfo`() {
        val sessionInfo = TestUtils.createSessionInfo()
        val result = flowInfoMapper.flowInfos(sessionInfo)
        assertEquals(1, result.size)
        assertEquals(
            "permit in $PROTOCOL from 192.168.0.2 5022 to 192.168.0.1 5010",
            result.first().flowDescriptions.first()
        )
    }

    @Test
    fun `extractPortList should return values taken from ranges and ports`() {
        val portSpecs = PortsSpec().ranges(
            listOf(
                PortsSpecRangesInner().from(10).to(15),
                PortsSpecRangesInner().from(12).to(18)
            )
        ).ports(listOf(19, 20))
        val expected = IntRange(10, 20).toList()
        assertEquals(expected, flowInfoMapper.extractPortList(portSpecs))
    }

    @Test
    fun `extractPortPairs should return cartesian product of uePorts and asPorts`() {
        val expected = listOf(Pair(1, 3), Pair(1, 4), Pair(2, 3), Pair(2, 4))
        val result = flowInfoMapper.extractPortsPairs(IntRange(1, 2), IntRange(3, 4))
        assertEquals(expected, result)
    }

    @Test
    fun `flowInfos should return a list of flow descriptions for each Pair(uePort, asPort)`() {
        val sessionInfo = TestUtils.createSessionInfo()
            .qos(QosProfile.L)
            .uePorts(PortsSpec().ports(listOf(4)))
            .asPorts(
                PortsSpec().ranges(
                    listOf(
                        PortsSpecRangesInner().from(1).to(2)
                    )
                ).ports(listOf(3))
            )
        val result = flowInfoMapper.flowInfos(sessionInfo)
        assertEquals(3, result.size)
        assertEquals(
            1,
            result.filter { it.flowDescriptions.contains("permit in $PROTOCOL from 192.168.0.2 4 to 192.168.0.1 2") }.size
        )
    }

}
