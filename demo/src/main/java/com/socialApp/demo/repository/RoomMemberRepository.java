package com.socialApp.demo.repository;

import com.socialApp.demo.entity.RoomMember;
import com.socialApp.demo.entity.RoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMemberId> {
}
