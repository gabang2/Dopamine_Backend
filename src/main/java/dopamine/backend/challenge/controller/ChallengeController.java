package dopamine.backend.challenge.controller;

import dopamine.backend.challenge.request.ChallengeEditDTO;
import dopamine.backend.challenge.request.ChallengeRequestDTO;
import dopamine.backend.challenge.response.ChallengeResponseDTO;
import dopamine.backend.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    /**
     * 챌린지 생성
     * @param challengeRequestDTO
     */
    @PostMapping("/challenges")
    public void createChallenge(@Valid @RequestBody ChallengeRequestDTO challengeRequestDTO){
        challengeService.createChallenge(challengeRequestDTO);
    }

    /**
     * 챌린지 삭제
     * @param challengeId
     */
    @DeleteMapping("/challenges/{challengeId}")
    public void deleteChallenge(@PathVariable Long challengeId){
        challengeService.deleteChallenge(challengeId);
    }

    /**
     * 챌린지 조회
     * @param challengeId
     * @return
     */
    @GetMapping("/challenges/{challengeId}")
    public ChallengeResponseDTO getChallenge(@PathVariable Long challengeId){
        return challengeService.getChallenge(challengeId);
    }

    @PutMapping("/challenges/{challengeId}")
    public void editChallenge(@PathVariable Long challengeId, @RequestBody ChallengeEditDTO challengeEditDTO){
        challengeService.editChallenge(challengeId, challengeEditDTO);
    }
}
