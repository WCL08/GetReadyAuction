package com.example.getreadyauction.dto.auction;

import com.example.getreadyauction.entity.Auction;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class AuctionRequestDto {

    private String title;
    private String category;
    private String content;
    private int minPrice;
    private String deadline;
}
