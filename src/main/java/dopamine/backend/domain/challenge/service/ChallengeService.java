package dopamine.backend.domain.challenge.service;

import dopamine.backend.domain.challenge.entity.Challenge;
import dopamine.backend.domain.challenge.mapper.ChallengeMapper;
import dopamine.backend.domain.challenge.repository.ChallengeCustomRepository;
import dopamine.backend.domain.challenge.repository.ChallengeRepository;
import dopamine.backend.domain.challenge.request.ChallengeEditDTO;
import dopamine.backend.domain.challenge.request.ChallengeRequestDTO;
import dopamine.backend.domain.challenge.response.ChallengeResponseDTO;
import dopamine.backend.domain.challengemember.entity.ChallengeMember;
import dopamine.backend.domain.challengemember.repository.ChallengeMemberRepository;
import dopamine.backend.domain.member.entity.Member;
import dopamine.backend.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeCustomRepository challengeCustomRepository;
    private final ChallengeMemberRepository challengeMemberRepository;

    private final ChallengeMapper challengeMapper;

    private final MemberService memberService;

    public Challenge verifiedChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId).orElseThrow(() -> new RuntimeException("존재하지 않는 챌린지입니다."));
    }

    /**
     * 챌린지 생성
     * @param challengeRequestDTO
     */
    public void createChallenge(ChallengeRequestDTO challengeRequestDTO) {

        Challenge challenge = challengeMapper.challengeRequestDtoToChallenge(challengeRequestDTO);

        challengeRepository.save(challenge);
    }

    /**
     * 챌린지 삭제
     * @param challengeId
     */
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = verifiedChallenge(challengeId);

        challenge.changeDelYn(true);
    }

    /**
     * 챌린지 완전 삭제 (DB)
     * @param challengeId
     */
    public void deleteChallengeHard(Long challengeId) {
        Challenge challenge = verifiedChallenge(challengeId);

        challengeRepository.delete(challenge);
    }

    /**
     * 챌린지 조회
     * @param challengeId
     * @return
     */
    @Transactional(readOnly = true)
    public ChallengeResponseDTO getChallenge(Long challengeId) {
        Challenge challenge = verifiedChallenge(challengeId);

        ChallengeResponseDTO challengeResponseDTO = challengeMapper.challengeToChallengeResponseDTO(challenge);

        return challengeResponseDTO;
    }

    /**
     * 챌린지 수정
     * @param challengeId
     * @param challengeEditDTO
     */
    public void editChallenge(Long challengeId, ChallengeEditDTO challengeEditDTO) {
        Challenge challenge = verifiedChallenge(challengeId);

        challenge.changeChallenge(challengeEditDTO);
    }

    /**
     * 오늘의 챌린지
     * @param userId
     * @return
     */
    public List<ChallengeResponseDTO> todayChallenge(Long userId) {

        Member member = memberService.verifiedMember(userId);
        List<ChallengeResponseDTO> challengeResponseList;

        // 기존에 챌린지 받은 적 없음
        LocalDateTime existRefreshDate = member.getChallengeRefreshDate();
        if(existRefreshDate == null){
            List<Challenge> todayChallenges = challengeCustomRepository.getTodayChallenges(null);
            challengeResponseList = getChallengeResponseList(todayChallenges);

            todayChallenges.stream().forEach((challenge) -> new ChallengeMember(member, challenge));
        }

        else {
            LocalDateTime today = LocalDateTime.now();

            String todayInfo = getFormatDate(today);
            String existInfo = getFormatDate(existRefreshDate);

            List<ChallengeMember> challengeMembers = member.getChallengeMembers();
            List<Challenge> exitChallenge = challengeMembers.stream().map(challengeMember -> challengeMember.getChallenge()).collect(Collectors.toList());

            // 갱신일자가 오늘이 아닐 경우, 새로운 챌린지 발급
            if(!todayInfo.equals(existInfo)){
                List<Challenge> todayChallenges = challengeCustomRepository.getTodayChallenges(exitChallenge);

                // 기존 연관관계 삭제
                for(int i = 0; i < challengeMembers.size(); i++){
                    challengeMembers.get(i).deleteChallengeMember();
                }
                challengeMemberRepository.deleteAllInBatch(challengeMembers);

                // 오늘의 챌린지 생성
                challengeResponseList = getChallengeResponseList(todayChallenges);

                todayChallenges.stream().forEach((challenge) -> new ChallengeMember(member, challenge));
            }
            // 조회
            else {
                challengeResponseList = getChallengeResponseList(exitChallenge);
            }
        }
        member.setChallengeRefreshDate(LocalDateTime.now());

        return challengeResponseList;
    }

    private static String getFormatDate(LocalDateTime today) {
        return today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
    }

    private List<ChallengeResponseDTO> getChallengeResponseList(List<Challenge> challenges) {
        return challenges.stream().map(challengeMapper::challengeToChallengeResponseDTO).collect(Collectors.toList());
    }
}
