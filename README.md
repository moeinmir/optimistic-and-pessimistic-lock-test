# Optimistic And Pessimistic Lock Test 

### We have a account entity class that has a field annotated with @version: 
- responsible for keeping the version and prevent synchronous data modification

```angular2html
    @Version
    private int version;

```

### In repository level the possibility of locking the access to the row is provided

```angular2html

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdWithPessimisticLock(@Param("id") Long id);

```

### Here we test optimistic lock using java threads and synchronized and shared lock object

```angular2html
    @Test
    public void testOptimisticLockPreventingSaveWhenItShould() throws InterruptedException {
        CustomUser optUser = CustomUser.createNewUser("optuser", "optuser", "optuser", "password");
        customUserRepository.save(optUser);
        Account optAccount = Account.createNewAccountWithBalanceForTheTest(optUser);
        accountRepository.save(optAccount);
        Long optUserId = optAccount.getAccountInformation().getId();
        Long optAccountId = optAccount.getAccountInformation().getId();
        Object lock = new Object();
        AtomicBoolean transactionInFirstThreadDone = new AtomicBoolean(false);
        Thread firstThread = new Thread(() -> {
            Account optAccountInFirstThread = accountRepository.findById(optAccountId).orElse(null);
            synchronized (lock) {
                Transaction transactionInFirstThread = optAccountInFirstThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
                transactionInFirstThreadDone.set(true);
                lock.notify();
                try {
                    lock.wait();  // Wait for secondThread to finish its work
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    transactionRepository.save(transactionInFirstThread);
                    accountRepository.save(optAccountInFirstThread);
                } catch (Exception e) {
                    System.out.println("the transaction in first thread could not be saved as we expected");
                    System.out.println(e.getMessage());
                }
            }
        });

        Thread secondThread = new Thread(() -> {
            Account optAccountInSecondThread = accountRepository.findById(optAccountId).orElse(null);
            synchronized (lock) {
                while (!transactionInFirstThreadDone.get()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Transaction transactionInSecondThread = optAccountInSecondThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
                transactionRepository.save(transactionInSecondThread);
                accountRepository.save(optAccountInSecondThread);
                System.out.println("transaction in second thread has been saved as we expected");
                lock.notify();
            }
        });
        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();
    }


```

### Here we test the pessimistic lock:

```angular2html

    @Test
    public void testPessimisticLockWithWithExecutor() throws InterruptedException {
        CustomUser optUser = CustomUser.createNewUser("optuser", "optuser", "optuser", "password");
        customUserRepository.save(optUser);
        Account optAccount = Account.createNewAccountWithBalanceForTheTest(optUser);
        accountRepository.save(optAccount);
        Long optUserId = optAccount.getAccountInformation().getId();
        Long optAccountId = optAccount.getAccountInformation().getId();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Runnable firstTask = () -> {
            System.out.println("account balance after two transaction");
            Account optAccountInFirstThread = accountRepository.findByIdWithPessimisticLock(optAccountId).orElse(null);
            Transaction transactionInFirstThread = optAccountInFirstThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
            transactionRepository.save(transactionInFirstThread);
            accountRepository.save(optAccountInFirstThread);
        };
        Runnable secondTask = () -> {
            System.out.println("account balance after two transaction");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                Account optAccountInSecondThread = accountRepository.findByIdWithPessimisticLock(optAccountId).orElse(null);
                if (optAccountInSecondThread == null) {
                    throw new RuntimeException();
                }
            }
            catch (Exception e) {
                System.out.println("we could not access the account because first thread locked it");
                System.out.println(e.getMessage());
            }
        };
        executor.submit(firstTask);
        executor.submit(secondTask);
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
    }

```