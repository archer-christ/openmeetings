/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.webservice;

import static org.apache.openmeetings.util.OmFileHelper.getDefaultProfilePicture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.apache.openmeetings.db.dto.basic.ServiceResult;
import org.apache.openmeetings.db.dto.file.FileItemDTO;
import org.apache.openmeetings.db.dto.room.RoomDTO;
import org.apache.openmeetings.db.dto.room.RoomFileDTO;
import org.apache.openmeetings.db.entity.file.BaseFileItem;
import org.apache.openmeetings.db.entity.room.Room;
import org.junit.Test;

public class TestRoomService extends AbstractWebServiceTest {
	private static final long CAPACITY = 666L;

	@Test
	public void testExternal() {
		ServiceResult sr = login();
		String extId = UUID.randomUUID().toString();
		Room.Type type = Room.Type.presentation;
		String name = "Unit Test Ext Room";
		String comment = "Unit Test Ext Room Comments";
		RoomDTO r = new RoomDTO();
		r.setType(type);
		r.setName(name);
		r.setComment(comment);
		r.setCapacity(CAPACITY);
		RoomDTO room = getClient(getRoomUrl()).path(String.format("/%s/%s/%s", type, UNIT_TEST_EXT_TYPE, extId))
				.query("sid", sr.getMessage())
				.query("room", r.toString())
				.get(RoomDTO.class);
		assertNotNull("Valid room should be returned", room);
		assertNotNull("Room ID should be not empty", room.getId());

		RoomDTO room1 = getClient(getRoomUrl()).path(String.format("/%s/%s/%s", Room.Type.presentation, UNIT_TEST_EXT_TYPE, extId))
				.query("sid", sr.getMessage())
				.get(RoomDTO.class);
		assertNotNull("Valid room should be returned", room1);
		assertEquals("Same Room should be returned", room.getId(), room1.getId());
	}

	@Test
	public void testCreate1() {
		String extId = UUID.randomUUID().toString();
		Room.Type type = Room.Type.presentation;
		String name = "Unit Test Ext Room1";
		String comment = "Unit Test Ext Room Comments1";
		RoomDTO r = new RoomDTO();
		r.setType(type);
		r.setName(name);
		r.setComment(comment);
		r.setCapacity(CAPACITY);
		r.setExternalType(UNIT_TEST_EXT_TYPE);
		r.setExternalId(extId);

		createAndValidate(r);
	}

	@Test
	public void testCreate2() {
		Room.Type type = Room.Type.presentation;
		String name = "Unit Test Ext Room2";
		String comment = "Unit Test Ext Room Comments2";
		RoomDTO r = new RoomDTO();
		r.setType(type);
		r.setName(name);
		r.setComment(comment);
		r.setCapacity(CAPACITY);

		createAndValidate(r);
	}

	@Test
	public void testCreateWithFiles1() {
		Room.Type type = Room.Type.presentation;
		String name = "Unit Test Ext Room3";
		String comment = "Unit Test Ext Room Comments3";
		RoomDTO r = new RoomDTO();
		r.setType(type);
		r.setName(name);
		r.setComment(comment);
		r.setCapacity(CAPACITY);
		RoomFileDTO rf = new RoomFileDTO();
		rf.setFileId(-666L); //not existent
		r.getFiles().add(rf);

		CallResult<RoomDTO> res = createAndValidate(r);
		assertTrue("No room files should be added", res.getObj().getFiles().isEmpty());
	}

	@Test
	public void testCreateWithFiles2() throws IOException {
		//lets create real file
		CallResult<FileItemDTO> fileCall = createVerifiedFile(getDefaultProfilePicture(), "img.jpg", BaseFileItem.Type.Image);

		Room.Type type = Room.Type.presentation;
		String name = "Unit Test Ext Room4";
		String comment = "Unit Test Ext Room Comments4";
		RoomDTO r = new RoomDTO();
		r.setType(type);
		r.setName(name);
		r.setComment(comment);
		r.setCapacity(CAPACITY);

		RoomFileDTO rf = new RoomFileDTO();
		rf.setFileId(fileCall.getObj().getId()); //not existent
		r.getFiles().add(rf);

		CallResult<RoomDTO> res = createAndValidate(fileCall.getSid(), r);
		assertFalse("Room files should NOT be empty", res.getObj().getFiles().isEmpty());
	}
}
