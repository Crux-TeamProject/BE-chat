package com.project.crux.crew.repository;


import com.project.crux.crew.domain.CrewMember;
import com.project.crux.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    List<CrewMember> findAllByMember(Member member);
}
