package ovh.paulem.buildscript;

import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;

public class NewGithubChangelog {
    public static String getChangelog() {
        try {
            GitHub gitHub = new GitHubBuilder()
                    .build();

            GHRepository repository = gitHub.getRepository("Paulem79/Named-Villagers");

            StringBuilder changelog = new StringBuilder("This version is uploaded automatically by GitHub Actions.")
                    .append("\n\nChangelog:");

            List<GHCommit> commits = repository.listCommits().toList();

            @Nullable GHCommit lastPublish = null;

            for (GHCommit commit : commits) {
                String message = commit.getCommitShortInfo().getMessage();
                String hash = commit.getSHA1();
                String commit_url = commit.getHtmlUrl().toString();

                if (message.contains("PUBLISH") && getWorkflowRun(repository, commit) == GHWorkflowRun.Conclusion.SUCCESS && commits.indexOf(commit) != 0) {
                    lastPublish = commit;
                    break;
                }
                else if(message.contains("PUBLISH")) {
                    message = message.replace("PUBLISH", "");
                }

                changelog
                        .append("\n")
                        .append("- [")
                        .append(hash, 0, 7)
                        .append("](")
                        .append(commit_url)
                        .append(") ")
                        .append(message);
            }

            if(lastPublish != null) {
                changelog
                        .append("\n\n")
                        .append("Diff: ")
                        .append(repository.getCompare(lastPublish, commits.get(0)).getHtmlUrl().toString());
            }

            return changelog.toString(); // Traitez l'objet JSON selon vos besoins*/
        } catch (IOException e) {
            return "No changelog was specified.";
        }
    }

    private static GHWorkflowRun.Conclusion getWorkflowRun(GHRepository repository, GHCommit commit) throws IOException {
        List<GHWorkflowRun> runs = repository.queryWorkflowRuns().headSha(commit.getSHA1()).list().toList();

        if (runs.isEmpty()) {
            return GHWorkflowRun.Conclusion.FAILURE;
        }

        return runs.get(0).getConclusion();
    }
}