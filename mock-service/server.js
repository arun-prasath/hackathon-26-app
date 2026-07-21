const http = require("http");
const https = require("https");

const port = Number(process.env.PORT || 8080);
const openAiModel = process.env.OPENAI_MODEL || "gpt-4o-mini";

const user = {
  id: "demo-user-001",
  name: "Avinash",
  clientType: "Premium Banking Client",
  lastLogin: "16 Jul 2026 9:32m",
  totalAssets: "INR 12,34,567.89",
  totalLiabilities: "INR 58,504.40",
  savingsAccount: {
    name: "SAVINGS ACCOUNT - GROUP STAFF",
    maskedNumber: "**** 1234",
    balance: "INR 10,00,000.00",
    currency: "INR"
  },
  investments: "INR 0.00",
  insurancePolicies: "0",
  transactions: [
    {
      date: "08 Jul 2026",
      title: "UPI transfer received",
      subtitle: "From salary account",
      amount: "INR 75,000.00",
      credit: true,
      category: "Income"
    },
    {
      date: "06 Jul 2026",
      title: "NACH DR I W",
      subtitle: "Monthly debit",
      amount: "INR 12,400.00",
      credit: false,
      category: "Bills"
    },
    {
      date: "30 Jun 2026",
      title: "SAVING A/C CREDIT INTEREST",
      subtitle: "Quarterly interest",
      amount: "INR 1,243.00",
      credit: true,
      category: "Income"
    },
    {
      date: "25 Jun 2026",
      title: "Card payment",
      subtitle: "Credit card bill",
      amount: "INR 18,500.00",
      credit: false,
      category: "Card Payment"
    },
    {
      date: "22 Jun 2026",
      title: "UPI payment",
      subtitle: "Grocery store",
      amount: "INR 2,850.00",
      credit: false,
      category: "Groceries"
    },
    {
      date: "18 Jun 2026",
      title: "ATM withdrawal",
      subtitle: "Cash withdrawal",
      amount: "INR 5,000.00",
      credit: false,
      category: "Cash"
    },
    {
      date: "14 Jun 2026",
      title: "NEFT credit",
      subtitle: "Reimbursement received",
      amount: "INR 9,200.00",
      credit: true,
      category: "Income"
    },
    {
      date: "09 Jun 2026",
      title: "Utility bill payment",
      subtitle: "Electricity bill",
      amount: "INR 2,140.00",
      credit: false,
      category: "Bills"
    },
    {
      date: "03 Jun 2026",
      title: "UPI payment",
      subtitle: "Restaurant",
      amount: "INR 1,760.00",
      credit: false,
      category: "Dining"
    },
    {
      date: "29 May 2026",
      title: "Salary credit",
      subtitle: "Monthly salary",
      amount: "INR 1,25,000.00",
      credit: true,
      category: "Income"
    },
    {
      date: "24 May 2026",
      title: "SIP debit",
      subtitle: "Mutual fund investment",
      amount: "INR 10,000.00",
      credit: false,
      category: "Investments"
    },
    {
      date: "18 May 2026",
      title: "UPI payment",
      subtitle: "Fuel station",
      amount: "INR 3,400.00",
      credit: false,
      category: "Fuel"
    },
    {
      date: "11 May 2026",
      title: "IMPS transfer",
      subtitle: "Family transfer",
      amount: "INR 15,000.00",
      credit: false,
      category: "Transfers"
    },
    {
      date: "05 May 2026",
      title: "Cashback credit",
      subtitle: "Card offer cashback",
      amount: "INR 850.00",
      credit: true,
      category: "Rewards"
    }
  ],
  debitCards: [
    {
      name: "Employee Banking Platinum Debit Card",
      maskedNumber: "**** 4321",
      type: "Debit Card",
      status: "ACTIVE"
    }
  ],
  creditCards: [
    {
      name: "SC Smart Rewards Credit Card",
      maskedNumber: "**** 8842",
      type: "Credit Card",
      status: "ACTIVE"
    }
  ],
  creditCardStatements: [
    { date: "20 Jul 2026", merchant: "BigBasket", description: "Grocery order", amount: "INR 2,340.00", category: "Groceries" },
    { date: "18 Jul 2026", merchant: "Uber", description: "Airport ride", amount: "INR 1,250.00", category: "Transport" },
    { date: "16 Jul 2026", merchant: "Amazon", description: "Electronics accessory", amount: "INR 3,999.00", category: "Shopping" },
    { date: "15 Jul 2026", merchant: "Netflix", description: "Monthly subscription", amount: "INR 649.00", category: "Subscriptions" },
    { date: "12 Jul 2026", merchant: "Swiggy", description: "Dinner order", amount: "INR 780.00", category: "Dining" },
    { date: "09 Jul 2026", merchant: "Apollo Pharmacy", description: "Medicines", amount: "INR 1,120.00", category: "Health" },
    { date: "06 Jul 2026", merchant: "Shell", description: "Fuel", amount: "INR 3,200.00", category: "Fuel" },
    { date: "02 Jul 2026", merchant: "BookMyShow", description: "Movie tickets", amount: "INR 1,100.00", category: "Entertainment" },
    { date: "28 Jun 2026", merchant: "DMart", description: "Monthly groceries", amount: "INR 5,480.00", category: "Groceries" },
    { date: "25 Jun 2026", merchant: "Myntra", description: "Apparel purchase", amount: "INR 4,250.00", category: "Shopping" },
    { date: "22 Jun 2026", merchant: "Zomato", description: "Lunch order", amount: "INR 620.00", category: "Dining" },
    { date: "20 Jun 2026", merchant: "Airtel", description: "Mobile bill", amount: "INR 999.00", category: "Bills" },
    { date: "17 Jun 2026", merchant: "Ola", description: "Office commute", amount: "INR 440.00", category: "Transport" },
    { date: "14 Jun 2026", merchant: "Croma", description: "Home appliance", amount: "INR 8,999.00", category: "Shopping" },
    { date: "10 Jun 2026", merchant: "Cult Fit", description: "Gym renewal", amount: "INR 2,499.00", category: "Health" },
    { date: "06 Jun 2026", merchant: "HPCL", description: "Fuel", amount: "INR 2,850.00", category: "Fuel" },
    { date: "30 May 2026", merchant: "Reliance Fresh", description: "Groceries", amount: "INR 3,760.00", category: "Groceries" },
    { date: "27 May 2026", merchant: "IRCTC", description: "Train booking", amount: "INR 2,150.00", category: "Travel" },
    { date: "23 May 2026", merchant: "Starbucks", description: "Cafe", amount: "INR 540.00", category: "Dining" },
    { date: "19 May 2026", merchant: "Google Play", description: "App subscription", amount: "INR 299.00", category: "Subscriptions" },
    { date: "16 May 2026", merchant: "MakeMyTrip", description: "Hotel booking", amount: "INR 9,750.00", category: "Travel" },
    { date: "12 May 2026", merchant: "Decathlon", description: "Sports gear", amount: "INR 2,890.00", category: "Shopping" },
    { date: "08 May 2026", merchant: "BESCOM", description: "Electricity bill", amount: "INR 1,860.00", category: "Bills" },
    { date: "04 May 2026", merchant: "INOX", description: "Cinema snacks", amount: "INR 720.00", category: "Entertainment" }
  ],
  bankingProducts: [
    {
      id: "mf-growth-suite",
      name: "SC Invest Mutual Funds",
      category: "Investments",
      description: "Curated mutual fund options for long-term wealth creation, SIP investing, and goal-based portfolios.",
      interestRate: "Market linked returns; no guaranteed rate",
      minimumAmount: "SIP from INR 500 per month; lump sum from INR 5,000",
      tenure: "Open-ended; recommended horizon 3 years or more",
      eligibility: "Resident individual with completed KYC and active savings account",
      fees: "Expense ratio as per fund; exit load may apply by scheme",
      keyBenefits: [
        "Goal-based SIP and lump-sum investing",
        "Equity, debt, hybrid, and tax-saving ELSS choices",
        "Portfolio tracking inside mobile banking"
      ]
    },
    {
      id: "fd-secure-growth",
      name: "Secure Growth Fixed Deposit",
      category: "Deposits",
      description: "Fixed deposit for predictable returns with flexible tenure and maturity instructions.",
      interestRate: "Up to 7.25% p.a.; senior citizen benefit up to 0.50% p.a. extra",
      minimumAmount: "INR 10,000",
      tenure: "7 days to 10 years",
      eligibility: "Resident individual, NRI, or eligible entity as per bank policy",
      fees: "Premature withdrawal penalty may apply",
      keyBenefits: [
        "Guaranteed returns at booked rate",
        "Auto-renewal and monthly/quarterly interest payout options",
        "Can be opened from existing savings account"
      ]
    },
    {
      id: "home-loan-prime",
      name: "Prime Home Loan",
      category: "Loans",
      description: "Home loan for purchase, construction, balance transfer, or top-up subject to credit assessment.",
      interestRate: "Starting from 8.60% p.a. floating",
      minimumAmount: "INR 5,00,000",
      tenure: "Up to 30 years",
      eligibility: "Salaried or self-employed customers with income, property, and credit checks",
      fees: "Processing fee up to 0.50% of loan amount plus taxes",
      keyBenefits: [
        "Long repayment tenure",
        "Balance transfer and top-up options",
        "Digital document tracking for application status"
      ]
    },
    {
      id: "personal-loan-instant",
      name: "Instant Personal Loan",
      category: "Loans",
      description: "Unsecured loan for travel, education, medical, wedding, or other personal needs.",
      interestRate: "Starting from 10.99% p.a.",
      minimumAmount: "INR 50,000",
      tenure: "12 to 60 months",
      eligibility: "Pre-approved or eligible salaried/self-employed customer based on credit policy",
      fees: "Processing fee up to 2.00% plus taxes; foreclosure charges may apply",
      keyBenefits: [
        "No collateral required",
        "Fast disbursal for eligible customers",
        "Fixed EMI repayment"
      ]
    },
    {
      id: "car-loan-drive",
      name: "DriveEasy Car Loan",
      category: "Loans",
      description: "Loan for new or used car purchase with flexible repayment options.",
      interestRate: "Starting from 9.25% p.a.",
      minimumAmount: "INR 1,00,000",
      tenure: "12 to 84 months",
      eligibility: "Resident individual with valid income proof and credit approval",
      fees: "Processing fee up to 1.00% plus taxes; documentation charges may apply",
      keyBenefits: [
        "Funding for new and used cars",
        "Flexible EMI options",
        "Quick approval for eligible customers"
      ]
    }
  ],
  recentEvents: []
};

function sendJson(response, statusCode, body) {
  response.writeHead(statusCode, {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
    "Cache-Control": "no-store"
  });
  response.end(JSON.stringify(body, null, 2));
}

const server = http.createServer((request, response) => {
  console.log(`[${new Date().toISOString()}] ${request.method} ${request.url}`);
  if (request.method === "OPTIONS") {
    response.writeHead(204, {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
    });
    response.end();
    return;
  }

  if (request.method === "GET" && request.url === "/health") {
    sendJson(response, 200, { status: "ok" });
    return;
  }

  if (request.method === "GET" && request.url === "/api/user/demo") {
    sendJson(response, 200, user);
    return;
  }

  if (request.method === "GET" && request.url === "/api/model/manifest") {
    sendJson(response, 200, {
      modelId: "sc-edge-router-v1",
      version: "1.0.0",
      downloadUrl: "http://localhost:8080/api/model/package",
      sizeBytes: 1048576,
      sha256: "demo-sha256-placeholder",
      minSdk: 33,
      minFreeStorageBytes: 104857600,
      capabilities: ["intent_classification", "faq_answering", "routing"]
    });
    return;
  }

  if (request.method === "GET" && request.url === "/api/model/package") {
    const buffer = Buffer.alloc(1024 * 1024, "SC_EDGE_ROUTER_V1_DEMO_MODEL");
    response.writeHead(200, {
      "Content-Type": "application/octet-stream",
      "Content-Length": buffer.length,
      "Cache-Control": "no-store"
    });
    response.end(buffer);
    return;
  }

  if (request.method === "POST" && request.url === "/api/ai/chat") {
    readJsonBody(request)
      .then((body) => {
        console.log(
          `[AI] queryLength=${(body.query || "").length}, messages=${Array.isArray(body.messages) ? body.messages.length : 0}, model=${openAiModel}`
        );
        console.log(body.query);
        return callOpenAi(body.query || "", body.customerContext || {}, body.messages || []);
      })
      .then((answer) => {
        console.log(`[AI] answerLength=${answer.length}`);
        sendJson(response, 200, {
          route: "BACKEND_AI",
          provider: "openai",
          model: openAiModel,
          answer
        });
      })
      .catch((error) => {
        console.error("[AI] error", error);
        sendJson(response, 500, {
          route: "BACKEND_AI",
          provider: "openai",
          model: openAiModel,
          error: error.message
        });
      });
    return;
  }

  if (request.method === "POST" && request.url === "/api/service-requests/address") {
    readJsonBody(request)
      .then((body) => {
        const serviceRequestNumber = `SR${Date.now().toString().slice(-8)}`;
        const address = body.address || {};
        const event = {
          type: "ADDRESS_CHANGE",
          title: "Address change requested",
          description: `Service request ${serviceRequestNumber} created for ${address.city || "new address"}.`,
          serviceRequestNumber,
          createdAt: new Date().toISOString()
        };
        user.recentEvents.unshift(event);
        console.log(`[SERVICE] Address change request created ${serviceRequestNumber}`);
        sendJson(response, 200, {
          serviceRequestNumber,
          message: `Your address change request has been submitted. Service request number: ${serviceRequestNumber}.`,
          event
        });
      })
      .catch((error) => {
        console.error("[SERVICE] address change error", error);
        sendJson(response, 500, { error: error.message });
      });
    return;
  }

  if (request.method === "POST" && request.url === "/api/cards/credit/block") {
    user.creditCards[0].status = "BLOCKED";
    const event = {
      type: "CARD_BLOCKED",
      title: "Credit card blocked",
      description: `${user.creditCards[0].name} ${user.creditCards[0].maskedNumber} is now BLOCKED.`,
      createdAt: new Date().toISOString()
    };
    user.recentEvents.unshift(event);
    console.log(`[CARD] Credit card blocked ${user.creditCards[0].maskedNumber}`);
    sendJson(response, 200, { message: event.description, user, event });
    return;
  }

  if (request.method === "POST" && request.url === "/api/cards/credit/unblock") {
    user.creditCards[0].status = "ACTIVE";
    const event = {
      type: "CARD_UNBLOCKED",
      title: "Credit card unblocked",
      description: `${user.creditCards[0].name} ${user.creditCards[0].maskedNumber} is now ACTIVE.`,
      createdAt: new Date().toISOString()
    };
    user.recentEvents.unshift(event);
    console.log(`[CARD] Credit card unblocked ${user.creditCards[0].maskedNumber}`);
    sendJson(response, 200, { message: event.description, user, event });
    return;
  }

  sendJson(response, 404, { error: "Not found" });
});

server.listen(port, "0.0.0.0", () => {
  console.log(`SC Mobile mock service running on http://0.0.0.0:${port}`);
  console.log("Android emulator URL: http://10.0.2.2:8080/api/user/demo");
  console.log("Physical phone URL: use your laptop IP, for example http://192.168.x.x:8080/api/user/demo");
});

function readJsonBody(request) {
  return new Promise((resolve, reject) => {
    let data = "";
    request.on("data", (chunk) => {
      data += chunk;
      if (data.length > 1024 * 1024) {
        reject(new Error("Request body too large"));
        request.destroy();
      }
    });
    request.on("end", () => {
      try {
        resolve(data ? JSON.parse(data) : {});
      } catch {
        reject(new Error("Invalid JSON body"));
      }
    });
    request.on("error", reject);
  });
}

function callOpenAi(query, customerContext, messages) {
  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    return Promise.resolve(
      "Backend AI proxy is running, but OPENAI_API_KEY is not set on the mock service. Set it and restart npm start."
    );
  }

  const payload = JSON.stringify({
    model: openAiModel,
    messages: [
      {
        role: "system",
        content:
          [
            "You are a cautious mobile banking assistant for a demo.",
            "Use the masked customer context only for helpful explanation.",
            "Do not request or reveal secrets, OTPs, full account numbers, or credentials.",
            "For transactions, security changes, fraud, complaints, or account changes, say backend authorization is required.",
            "Keep responses concise and conversational.",
            `Masked customer context: ${JSON.stringify(customerContext)}`
          ].join(" ")
      },
      ...messages.slice(-10).map((message) => ({
        role: message.role === "user" ? "user" : "assistant",
        content: String(message.text || "").slice(0, 1000)
      })),
      {
        role: "user",
        content: query
      }
    ],
    temperature: 0.2
  });

  const options = {
    hostname: "api.openai.com",
    path: "/v1/chat/completions",
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
      "Content-Length": Buffer.byteLength(payload)
    }
  };

  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      let data = "";
      res.on("data", (chunk) => {
        data += chunk;
      });
      res.on("end", () => {
        try {
          const parsed = JSON.parse(data);
          const answer = parsed.choices?.[0]?.message?.content;
          if (!answer) {
            reject(new Error(parsed.error?.message || `OpenAI returned HTTP ${res.statusCode}`));
            return;
          }
          resolve(answer);
        } catch {
          reject(new Error(`OpenAI response parse failed: ${data.slice(0, 200)}`));
        }
      });
    });
    req.on("error", reject);
    req.write(payload);
    req.end();
  });
}
