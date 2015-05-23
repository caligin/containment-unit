package org.anima.ptsd;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Image;
import java.util.List;
import net.emaze.dysfunctional.Reductions;
import net.emaze.dysfunctional.dispatching.logic.Predicate;

public class DockerImage {

    final String tag;

    public DockerImage(String tag) {
        this.tag = tag;
    }

    public static DockerImage fromIndex(DockerClient docker, final String tag) {
        try {
            final List<Image> images = docker.listImages();
            final boolean alreadyPulled = Reductions.any(images, new Predicate<Image>() {

                @Override
                public boolean accept(Image image) {
                    return image.repoTags().contains(tag);
                }
            });
            if (!alreadyPulled) {
                docker.pull(tag);
            }
            return new DockerImage(tag);
        } catch (DockerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
