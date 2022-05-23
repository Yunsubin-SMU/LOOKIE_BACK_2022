package com.example.lookie.grouprequest.service;

import com.example.lookie.group.domain.Group;
import com.example.lookie.group.domain.Question;
import com.example.lookie.group.domain.QuestionAnswer;
import com.example.lookie.group.domain.QuestionAnswerRequestDto;
import com.example.lookie.group.repository.GroupRepository;
import com.example.lookie.group.repository.QuestionRepository;
import com.example.lookie.grouprequest.domain.Department;
import com.example.lookie.grouprequest.domain.GroupRequest;
import com.example.lookie.grouprequest.domain.RequestStatus;
import com.example.lookie.grouprequest.repository.GroupRequestRepository;
import com.example.lookie.member.domain.Member;
import com.example.lookie.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor

public class GroupRequestServiceImpl implements GroupRequestService{
    private final GroupRepository groupRepository;
    private final GroupRequestRepository groupRequestRepository;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;

    /**
     * GroupRequest 생성(그룹 가입 요청)
     */
    @Transactional
    @Override
    /*
    public Long makeGroupRequest(String ownerEmail, Member member, Group group,
                                 Department department, QuestionAnswer...questionAnswers) {

        GroupRequest groupRequest = GroupRequest.createGroupRequest(member, group, department, questionAnswers);
        return groupRequestRepository.save(groupRequest).getId();
    }*/
    public Long createGroupRequest(String requestMemberEmail, String groupName, Department department
            , List<QuestionAnswerRequestDto> questionAnswerRequestDtos) {
        Member member = checkRequestMemberEmail(requestMemberEmail);

        Group group = checkRequestGroupName(groupName);

        checkDuplicateRequest(requestMemberEmail, groupName);

        List<QuestionAnswer> questionList = questionAnswerRequestDtos.stream()
                .map(a -> {
                    Question question = questionRepository.findById(a.getQuestionId()).get();
                    QuestionAnswer questionAnswer = QuestionAnswer.createRequestQuestion(question, a.getTitle(), a.getAnswer());
                    return questionAnswer;
                })
                .collect(Collectors.toList());

        GroupRequest groupRequest = GroupRequest.createGroupRequest(member, group, department, (QuestionAnswer[])questionList.toArray());

        return groupRequestRepository.save(groupRequest).getId();
    }

    /**
     * GroupRequest 조회
     */
    @Override
    /*public List<GroupRequest> checkGroupRequest(String ownerEmail) {
        groupRequestRepository.findByOwnerEmail(ownerEmail).orElseThrow(()-> {
            throw new IllegalArgumentException("회원님이 만든 그룹이 존재하지 않습니다.");
        });

        return groupRequestRepository.findAll();
    }*/
    public List<GroupRequest> findGroupRequestAll(String ownerEmail){
        Group group = groupRepository.findByOwnerEmail(ownerEmail).orElseThrow(()->{
            throw new IllegalArgumentException("생성하신 그룹은 없습니다.");
        });
        validateRequest(ownerEmail, group.getOwnerEmail());
        return group.getGroupRequestList();
    }

    @Override
    public List<QuestionAnswer> findGroupRequestQuestionAnswer(Long groupRequestId, String ownerEmail) {
        GroupRequest groupRequest = groupRequestRepository.findFetchJoinById(groupRequestId).orElseThrow(() -> {
            throw new IllegalArgumentException("찾으시려는 요청은 존재하지 않습니다.");
        });
        validateRequest(ownerEmail, groupRequest.getGroup().getOwnerEmail());
        return groupRequest.getQuestionAnswerList();
    }

    /**
     * GroupRequest 수정
     */
    @Transactional
    @Override
    /*public void editRequest(String ownerEmail, GroupRequest groupRequest) {
        groupRequest = groupRequestRepository.findByOwnerEmail(ownerEmail).orElseThrow(()-> {
            throw new IllegalArgumentException("회원님이 만든 그룹이 존재하지 않습니다.");
        });

        groupRequest.changeRequestStatus(groupRequest.getRequestStatus());
    }*/
    public void acceptGroupRequest(Long groupRequestId, String ownerEmail) {
        GroupRequest groupRequest = groupRequestRepository.findFetchJoinById(groupRequestId).orElseThrow(() -> {
            throw new IllegalArgumentException("찾으시려는 요청은 존재하지 않습니다.");
        });
        validateRequest(ownerEmail, groupRequest.getGroup().getOwnerEmail());
        groupRequest.changeRequestStatus(RequestStatus.ACCEPT);
    }

    @Transactional
    @Override
    public void rejectGroupRequest(Long groupRequestId, String ownerEmail) {
        GroupRequest groupRequest = groupRequestRepository.findFetchJoinById(groupRequestId).orElseThrow(() -> {
            throw new IllegalArgumentException("찾으시려는 요청은 존재하지 않습니다.");
        });
        validateRequest(ownerEmail, groupRequest.getGroup().getOwnerEmail());
        groupRequest.changeRequestStatus(RequestStatus.REJECTION);
    }

    private Member checkRequestMemberEmail(String requestMemberEmail) {
        Member member = memberRepository.findByEmail(requestMemberEmail).orElseThrow(() -> {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        });
        return member;
    }

    private Group checkRequestGroupName(String groupName) {
        return groupRepository.findByName(groupName).orElseThrow(() -> {
            throw new IllegalArgumentException("없는 그룹입니다. ");
        });
    }

    private void checkDuplicateRequest(String requestMemberEmail, String groupName) {
        groupRequestRepository.findByMemberEmailAndGroupName(requestMemberEmail, groupName).ifPresent(a -> {
            throw new IllegalArgumentException("이미 신청한 기록이 있습니다. ");
        });
    }

    private void validateRequest(String ownerEmail, String requestGroupOwnerEmail) {
        if(ownerEmail.equals(requestGroupOwnerEmail)){
            throw new IllegalArgumentException("권한이 없습니다.");
        }
    }
}