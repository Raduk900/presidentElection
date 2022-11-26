package president.election.application.services;

import president.election.application.models.Candidate;
import president.election.application.models.CandidateVotes;
import president.election.application.repositories.CandidateRepo;
import president.election.application.repositories.VoteRepo;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.*;

/**
 * CandidateService implementation
 */
@Service("candidateService")
@Transactional
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepo candidateRepo;
    private final VoteRepo voteRepo;

    public CandidateServiceImpl(CandidateRepo candidateRepo, VoteRepo voteRepo) {
        this.candidateRepo = candidateRepo;
        this.voteRepo = voteRepo;
    }

    /**
     *
     * @return all candidates from mysql db. using JPA.
     */
    @Override
    public List<Candidate> findAll() {
        return candidateRepo.findAll();
    }

    /**
     *
     * @return One winning candidate if he has over 50 % of votes or 2 with most votes.
     * @throws SQLException
     */
    @Override
    public List<Candidate> getWinningCandidates() throws SQLException {
        List<Candidate> winnersList = new ArrayList<>();
        var candidates = voteRepo.getVotesPerCandidate();
        var mostVoted = topCandidates(candidates);
        if (mostVoted.size() > 2) {
            return winnersList;
        }else if(mostVoted.size()==2){
            winnersList.add(mostVoted.get(0).getCandidate());
            winnersList.add(mostVoted.get(1).getCandidate());
            return winnersList;
        }
        else if (mostVoted.size() == 1) {
            double candidateVotePercentage = ((double) mostVoted.get(0).getVotes() / totalVotes()) * 100;
            winnersList.add(mostVoted.get(0).getCandidate());
            if (candidateVotePercentage <= 50.0f) {
                candidates.remove(mostVoted);
                var leastVoted = topCandidates(candidates);
                if (leastVoted.size() > 1) {
                } else {
                    winnersList.add(leastVoted.get(0).getCandidate());
                }
            }
        }
        return winnersList;
    }

    /**
     *
     * @return number of votes submited.
     */
    public int totalVotes() {
        return voteRepo.findAll().size();
    }

    /**
     *
     * @param votes
     * @return Top voted candidate.
     */
    public List<CandidateVotes> topCandidates(List<CandidateVotes> votes) {
        Collections.sort(votes, new Comparator<CandidateVotes>() {
            @Override
            public int compare(CandidateVotes p1, CandidateVotes p2) {
                return p1.getVotes() - p2.getVotes();
            }
        });

        var MostVotedList = new ArrayList<CandidateVotes>();
        var MostVotedCandidate = votes.get(votes.size()-1);
        MostVotedList.add(MostVotedCandidate);
        votes.remove(MostVotedCandidate);
        for (var value : votes) {
            if(MostVotedCandidate.getVotes() == value.getVotes()) {
                MostVotedList.add(value);
            }
        }
        return MostVotedList;
    }
}
