package com.web.root.auction.service;
 
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.root.auction.dto.AuctionDTO;
import com.web.root.customer.dto.CartDTO;
import com.web.root.mybatis.auction.AuctionMapper;
import com.web.root.mybatis.post.PostMapper;

@Service
public class AuctionServiceImpl implements AuctionService{

	@Autowired
	public AuctionMapper auctionMapper;
	
	@Autowired
	public PostMapper postMapper;
	 
	
	// 경매 : 현황
	@Override
	public List<AuctionDTO> auctionView(Map<String, Object> map) {
		return auctionMapper.auctionView(map);
	}

	// 경매 : 입찰	
	@Override
	public int auctionBid(Map<String, Object> map) {
		int res = 0;
		try {
			res = auctionMapper.auctionBid(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	
	
	// 경매 : 삭제
	@Override
	public int deleteAuction(Map<String, Object> map) {
		int res = 0;
		try {
			res = auctionMapper.deleteAuction(Integer.parseInt(map.get("aucSeq").toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	
	// 경매 조회 
	public AuctionDTO getAuction(int aucSeq) {
		return auctionMapper.getAuction(aucSeq);
	}
	
	
	// 경매 카운트 다운 (어디에 집어 넣어야 하는가??)
	public void timeOver() {
		
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			
			@Override
			public void run() {	
				
				// 현재 시간
				LocalDateTime now = LocalDateTime.now();

				// 경매 종료 시간 (년, 월, 일, 시, 분, 초)
				LocalDateTime endTime = LocalDateTime.of(2023, 6, 15, 15, 41, 00);
							
				// now시간부터 endTime시간까지 시간차이(초단위)를 count에 담기
				long count = now.until(endTime, ChronoUnit.SECONDS);
				
				
				
				
				if(count > 0) {
					// 시간 형식 포맷
					// 24시간이 안넘은 값은 day로 쳐주지 않음
					LocalDateTime closestFullDaysStart = LocalDateTime.of(
							now.toLocalDate().plusDays(endTime.toLocalTime().compareTo(now.toLocalTime()) < 0 ? 1 : 0),
						    endTime.toLocalTime()
					);
					
					// LocalDate (Duration은 년, 월, 일의 날짜 차이를 다룬다)
					Duration duration = Duration.between(now, closestFullDaysStart);
					
					// LocalTime (Period는 시, 분, 초, 밀리초 시간 차이를 다룬다)
					Period period = Period.between(closestFullDaysStart.toLocalDate(), endTime.toLocalDate());
					
					// period로 일(day)표현 , duration으로 시,분,초 표현
					System.out.printf(
							"%d 일 "
							+ "%d 시 "
							+ "%d 분 "
							+ "%d 초",
							period.getDays(), 
							duration.toHoursPart(), 
							duration.toMinutesPart(), 
							duration.toSecondsPart()
					);
					System.out.println("");
					
				} else if(count == 0){
					System.out.println("실행 5");
					// 제일 높은 auc_price 가진 회원 데이터 넘기고
					Map<String, Object> map = new HashMap<String, Object>();
					CartDTO cartDTO = getSuccessfulBid(map);
					postMapper.auctionCart(cartDTO);
					// 나머지 회원들 데이터 삭제
					System.out.println("실행 6");
					auctionMapper.deleteParticipant();
					System.out.println("실행 7");
					timer.cancel();		
				} 
			}
		};	
		// now.getTime()='현재시간'부터 1초간격으로 run() 실행 
		timer.schedule(timerTask, new Date(), 1000);	
	}
	

	
	public CartDTO getSuccessfulBid(Map<String, Object> map) {
		  // 제일 높은 auc_price 가진 회원 데이터 가져오기
		  AuctionDTO auctionDTO = auctionMapper.getSuccessfulBid(map);
		  
		  
		  // CartDTO 객체 생성 및 값 설정
		  CartDTO cartDTO = new CartDTO();
		  cartDTO.setMemberSeq(auctionDTO.getMemberSeq());
		  cartDTO.setPostSeq(auctionDTO.getPostSeq());
		  
		  return cartDTO;
	}
	
//	public int deleteParticipant(Map<String, Object> map) {
//		AuctionDTO auctionDTO = auctionMapper.getSuccessfulBid(map);
//		int aucPrice = auctionDTO.getAucPrice();
//		
//		return aucPrice;
//	}
	
	

	
	
	
	
}
