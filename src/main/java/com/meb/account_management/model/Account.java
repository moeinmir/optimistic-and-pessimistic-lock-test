package com.meb.account_management.model;

import com.meb.account_management.constant.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal balance;
    private boolean isLocked;

    @Version
    private int version;

    @ManyToOne(fetch = FetchType.EAGER)
    private CustomUser owner;

    public static Account createNewAccount(CustomUser owner) {
        return Account.builder().balance(BigDecimal.ZERO).owner(owner).isLocked(false).build();
    }

    public Transaction addTransaction(BigDecimal amount, TransactionType type, Long requesterId) {

        if (type == TransactionType.DEBIT){
            if(!this.owner.getUserInformation().id.equals(requesterId)){
                return null;
            }
            if (this.balance.compareTo(amount) > 0){
                return null;
            }
            this.balance = this.balance.subtract(amount);
        }
        if (type == TransactionType.CREDIT){
            this.balance = this.balance.add(amount);
        }
        return Transaction.builder().amount(amount).type(type).account(this).build();
    }

    @Builder
    @Getter
    @Setter
    public static class AccountDto{
        public Long id;
        public BigDecimal balance;
        public Long ownerId;
        public Boolean isLocked;
    }

    public AccountDto getAccountInformation(){
        return AccountDto.builder().id(id).balance(this.balance).isLocked(this.isLocked).ownerId(this.owner.getUserInformation().id).build();
    }

}
