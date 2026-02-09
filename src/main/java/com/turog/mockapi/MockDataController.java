package com.turog.mockapi;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MockDataController {

    @GetMapping("/credit-accounts")
    public List<CreditAccount> getCreditAccounts() {
        return List.of(
                new CreditAccount("1", "Kayode Odole", "KO", "1829012982", "Lagos, Nigeria",
                        "NEW", 45000000.0, "NGN", "Corporate", 45000000.0, "1st July 2025 10:00 AM"),
                new CreditAccount("2", "Seyi Akamo", "SA", "17821086346", "Lagos, Nigeria",
                        "APPROVED", 62000000.0, "NGN", "Individual", 62000000.0, "1st July 2025 10:00 AM")
        );
    }

    @GetMapping("/investment-accounts")
    public List<InvestmentAccount> getInvestmentAccounts() {
        return List.of(
                new InvestmentAccount("1", "Kayode Odole", "KO", "4367383893",
                        "Mutual Funds", "LOW", 215000.0),
                new InvestmentAccount("2", "Kayode Odole", "KO", "4367383893",
                        "Mutual Funds", "MEDIUM", 215000.0)
        );
    }

    @GetMapping("/savings-accounts")
    public List<SavingsAccount> getSavingsAccounts() {
        return List.of(
                new SavingsAccount("1", "Kayode Odole", "KO", "5376457897588995",
                        "3273892992", "INACTIVE", 215000.0),
                new SavingsAccount("2", "Seyi Akamo", "SA", "4674884947588995",
                        "4367383893", "ACTIVE", 123000.0)
        );
    }

    @GetMapping("/merchants")
    public List<Category> getMerchants() {
        // SMS Messaging Category
        Configuration twilioConfig = new Configuration(
                "Twilio Inc.",
                "None defined",
                "**click to view**",
                "**click to view**",
                "**click to view**"
        );

        Merchant twilioSms = new Merchant(
                "1",
                "Twilio Inc.",
                "Messaging",
                "from ₦70/month",
                "https://workable-application-form.s3.amazonaws.com/advanced/production/5fdcc9206be75c0453544855/f88ea473-b8bf-9c6e-bb5a-c8c2a92273d8",
                "In the following, you will find an overview of the legal basis of the GDPR on which we base the processing of personal data. Please note that in addition to the provisions of the GDPR, national data protection provisions",
                List.of(
                        "In the following, you will find an overview",
                        "of the legal basis of the GDPR on which we base the processing of personal data.",
                        "Please note that in addition to the",
                        "provisions of the GDPR, national data protection provisions"
                ),
                List.of(
                        "1000-3000 messages for ₦70/month",
                        "1000-3000 messages for ₦70/month",
                        "Please note that in addition to the",
                        "1000-3000 messages for ₦70/month"
                ),
                twilioConfig
        );

        Merchant termii = new Merchant(
                "2",
                "Termii",
                "Messaging",
                "from ₦500",
                "https://raw.githubusercontent.com/drchibs/termii-nodejs/HEAD/images/termii.png",
                null,
                List.of(
                        "In the following, you will find an overview",
                        "of the legal basis of the GDPR on which we base the processing of personal data.",
                        "Please note that in addition to the"
                ),
                List.of(
                        "1000-3000 messages for ₦70/month",
                        "1000-3000 messages for ₦70/month"
                ),
                null
        );

        // Email Messaging Category
        Merchant omnisendEmail = new Merchant(
                "3",
                "Omnisend",
                "Messaging",
                "from ₦500",
                "https://wordpress.org/five-for-the-future/files/2019/09/Omnisend-logo-black.png",
                null, null, null, null
        );

        // Push Messaging Category
        Merchant twilioPush = new Merchant(
                "4",
                "Twilio Inc.",
                "Messaging",
                "from ₦70/month",
                "https://workable-application-form.s3.amazonaws.com/advanced/production/5fdcc9206be75c0453544855/f88ea473-b8bf-9c6e-bb5a-c8c2a92273d8",
                null, null, null, null
        );

        Merchant omnisendPush = new Merchant(
                "5",
                "Omnisend",
                "Messaging",
                "from ₦500",
                "https://wordpress.org/five-for-the-future/files/2019/09/Omnisend-logo-black.png",
                null, null, null, null
        );

        // Card Processors Category
        Merchant twilioCard = new Merchant(
                "6",
                "Twilio Inc.",
                "Messaging",
                "from ₦70/month",
                "https://workable-application-form.s3.amazonaws.com/advanced/production/5fdcc9206be75c0453544855/f88ea473-b8bf-9c6e-bb5a-c8c2a92273d8",
                null, null, null, null
        );

        return List.of(
                new Category("SMS Messaging", List.of(twilioSms, termii)),
                new Category("Email Messaging", List.of(omnisendEmail)),
                new Category("Push Messaging", List.of(twilioPush, omnisendPush)),
                new Category("Card Processors", List.of(twilioCard))
        );
    }

    @GetMapping("/merchant-groups")
    public List<MerchantGroup> getMerchantGroups() {
        return List.of(
                new MerchantGroup("all", "All Merchants"),
                new MerchantGroup("messaging", "Messaging"),
                new MerchantGroup("processing", "Processing"),
                new MerchantGroup("lookup-services", "Look-up Services"),
                new MerchantGroup("validation-services", "Validation Services")
        );
    }
}