package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.auditing.CurrentDateTimeProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto){

        //Save The subscription Object into the Db and return the total Amount that user has to pay
        Integer numberOfScreen=subscriptionEntryDto.getNoOfScreensRequired();
        Integer totalAmount=0;

        User user=userRepository.findById(subscriptionEntryDto.getUserId()).get();


        Subscription subscription= new Subscription();
        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());

        if(subscriptionEntryDto.getSubscriptionType().toString().equals("BASIC")){
            Integer amountWithNoOfScreenAmount=500+(200*numberOfScreen);
            totalAmount=amountWithNoOfScreenAmount;

        } else if (subscriptionEntryDto.getSubscriptionType().toString().equals("PRO")) {
            Integer amountWithNoOfScreenAmount=800+(250*numberOfScreen);
            totalAmount=amountWithNoOfScreenAmount;

        }else {
            Integer amountWithNoOfScreenAmount=1000+(350*numberOfScreen);
            totalAmount=amountWithNoOfScreenAmount;
        }

        subscription.setUser(user);
        subscription.setTotalAmountPaid(totalAmount);
        subscription.setNoOfScreensSubscribed(numberOfScreen);

        user.setSubscription(subscription);

        return totalAmount;
    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository
        User user=userRepository.findById(userId).get();
        if(user.getSubscription().getSubscriptionType().toString().equals("ELITE")){
            throw new Exception("Already the best Subscription");
        }

        Subscription subscription=user.getSubscription();
        Integer previousFair=subscription.getTotalAmountPaid();
        Integer currentFair;
        if(subscription.getSubscriptionType().equals(SubscriptionType.BASIC)){
            subscription.setSubscriptionType(SubscriptionType.PRO);
            currentFair =previousFair+300+(50*subscription.getNoOfScreensSubscribed());
        }else {
            subscription.setSubscriptionType(SubscriptionType.ELITE);
            currentFair=previousFair+200+(100*subscription.getNoOfScreensSubscribed());
        }

        subscription.setTotalAmountPaid(currentFair);
        user.setSubscription(subscription);
        subscriptionRepository.save(subscription);

        return currentFair-previousFair;
    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb
        List<Subscription> subscriptionList=subscriptionRepository.findAll();
        int total=0;
        for(Subscription subscription:subscriptionList){
            total+=subscription.getTotalAmountPaid();
        }
        return total;
    }

}
