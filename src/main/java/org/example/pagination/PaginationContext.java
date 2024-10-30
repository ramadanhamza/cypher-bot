package org.example.pagination;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class PaginationContext {

    @Getter
    @Setter
    private int currentPage;

    @Getter
    private List<MessageEmbed> embeds;

    public PaginationContext(int currentPage, List<MessageEmbed> embeds) {
        this.currentPage = currentPage;
        this.embeds = embeds;
    }
}
