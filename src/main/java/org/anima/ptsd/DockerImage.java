package org.anima.ptsd;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Image;
import java.util.List;

public class DockerImage {

    final String tag;

    public DockerImage(String tag) {
        this.tag = tag;
    }

    public static DockerImage fromIndex(DockerClient docker, String tag) {
        try {
            final List<Image> images = docker.listImages();
            boolean alreadyPulled = images.stream().anyMatch((image) -> image.repoTags().contains(tag));
            if (!alreadyPulled) {
                docker.pull(tag);
            }
            return new DockerImage(tag);
        } catch (DockerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
