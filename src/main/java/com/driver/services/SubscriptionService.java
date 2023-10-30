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
        int userId=subscriptionEntryDto.getUserId();
        User user=userRepository.findById(userId).get();
        int subscriptionFee=0;
        int screenFee=0;
        switch(subscriptionEntryDto.getSubscriptionType()){

            case BASIC:
                subscriptionFee=500;
                screenFee=200;
                break;
            case PRO:
                subscriptionFee=800;
                screenFee=250;
                break;
            case ELITE:
                subscriptionFee=1000;
                screenFee=350;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + subscriptionEntryDto.getSubscriptionType());
        }
        screenFee=screenFee*subscriptionEntryDto.getNoOfScreensRequired();
        int totalAmountPaid=subscriptionFee+screenFee;
        Subscription subscription=new Subscription();
        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());
        subscription.setStartSubscriptionDate(Calendar.getInstance().getTime());
        subscription.setTotalAmountPaid(totalAmountPaid);
        subscription.setUser(user);
        user.setSubscription(subscription);
        subscriptionRepository.save(subscription);
        return totalAmountPaid;
    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository
        User user=userRepository.findById(userId).get();
        Subscription subscription=user.getSubscription();
        if(subscription.getSubscriptionType().toString().equals("ELITE"))
            throw new Exception("Already the best Subscription");
        SubscriptionType subscriptionType=subscription.getSubscriptionType();
        int noofscreens=subscription.getNoOfScreensSubscribed();
        SubscriptionType newSubscriptionType=null;
        int subscriptionFee=0;
        int screenFee=0;
        switch(subscriptionType){

            case BASIC:
                newSubscriptionType=SubscriptionType.PRO;
                subscriptionFee=800;
                screenFee=250;
                break;
            case PRO:
                newSubscriptionType=SubscriptionType.ELITE;
                subscriptionFee=1000;
                screenFee=350;
                break;
        }
        int totalAmount=subscriptionFee+(screenFee*noofscreens);
        int oldAmount=subscription.getTotalAmountPaid();
        subscription.setSubscriptionType(newSubscriptionType);
        subscription.setTotalAmountPaid(totalAmount);
        user.setSubscription(subscription);
        subscriptionRepository.save(subscription);
        return totalAmount-oldAmount;
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
