package com.doot.app.utils

object TranslationEngine {
    fun translateForDemo(englishText: String, targetLang: String): String {
        val text = englishText.lowercase().trim()
        

        if (targetLang == "hi") {
            return when {

                text.contains("copy that") || text.contains("roger") -> "मुझे समझ आ गया"
                text.contains("over and out") -> "बात खत्म"
                text.contains("repeat message") -> "संदेश दोहराएं"
                text.contains("do you copy") -> "क्या आपको मेरी आवाज़ आ रही है?"
                text.contains("help") -> "मुझे तुरंत मदद चाहिए"
                text.contains("danger") || text.contains("hazard") -> "आगे खतरा है, सावधान रहें"
                text.contains("medical") -> "मेडिकल इमरजेंसी है"
                text.contains("abort") -> "मिशन रोक दो"
                text.contains("astrophase is ready") -> "टीम एस्ट्रोफेज तैयार है"
                text.contains("all systems go") -> "सभी सिस्टम तैयार हैं"
                text.contains("rocket launch") -> "रॉकेट लॉन्च शुरू हो गया है"
                text.contains("we have a problem") -> "हमें एक समस्या है"
                text.contains("connection established") -> "संपर्क स्थापित हो गया है"
                text.contains("orbit reached") -> "हम कक्षा में पहुंच गए हैं"
                text.contains("mission successful") -> "मिशन सफल रहा"

                // ==========================================
                // 2. CRITICAL EMERGENCIES & MEDICAL
                // ==========================================
                text.contains("call an ambulance") -> "एंबुलेंस बुलाओ"
                text.contains("call the police") -> "पुलिस को बुलाओ"
                text.contains("call the fire brigade") || text.contains("fire") -> "आग लगी है, फायर ब्रिगेड को बुलाओ"
                text.contains("i am injured") || text.contains("i'm injured") -> "मैं घायल हूँ"
                text.contains("he is injured") || text.contains("she is injured") -> "वह घायल है"
                text.contains("bleeding") -> "खून बह रहा है"
                text.contains("heart attack") -> "दिल का दौरा पड़ा है"
                text.contains("unconscious") || text.contains("fainted") -> "कोई बेहोश हो गया है"
                text.contains("cannot breathe") || text.contains("can't breathe") -> "सांस लेने में तकलीफ हो रही है"
                text.contains("need a doctor") -> "डॉक्टर की ज़रूरत है"
                text.contains("where is the hospital") -> "अस्पताल कहाँ है?"
                text.contains("first aid") -> "प्राथमिक चिकित्सा बॉक्स कहाँ है?"
                text.contains("i am trapped") || text.contains("we are trapped") -> "हम फँस गए हैं"
                text.contains("save me") || text.contains("save us") -> "हमें बचाओ"

                // ==========================================
                // 3. DISASTERS, RESCUE & SECURITY
                // ==========================================
                text.contains("earthquake") -> "भूकंप आया है, बाहर निकलो"
                text.contains("flood") || text.contains("water is rising") -> "बाढ़ आ गई है, पानी बढ़ रहा है"
                text.contains("building collapsed") -> "इमारत गिर गई है"
                text.contains("evacuate") -> "तुरंत जगह खाली करो"
                text.contains("stay calm") || text.contains("don't panic") -> "शांत रहें, घबराएं नहीं"
                text.contains("move back") || text.contains("step back") -> "पीछे हट जाओ"
                text.contains("is everyone safe") -> "क्या सभी सुरक्षित हैं?"
                text.contains("i am safe") -> "मैं सुरक्षित हूँ"
                text.contains("lost my family") || text.contains("lost my child") -> "मैं अपने परिवार से बिछड़ गया हूँ"
                text.contains("send rescue") || text.contains("send a team") -> "बचाव दल भेजो"
                text.contains("thief") || text.contains("robbery") -> "चोरी हुई है"
                text.contains("accident") -> "एक्सीडेंट हो गया है"

                // ==========================================
                // 4. TRAVEL, DIRECTIONS & LOCATION
                // ==========================================
                text.contains("where are you") -> "आप कहाँ हैं?"
                text.contains("i am here") -> "मैं यहाँ हूँ"
                text.contains("lost") && text.contains("way") -> "हम रास्ता भटक गए हैं"
                text.contains("where is the station") -> "स्टेशन कहाँ है?"
                text.contains("where is the airport") -> "एयरपोर्ट कहाँ है?"
                text.contains("i am coming") -> "मैं आ रहा हूँ"
                text.contains("i have reached") || text.contains("arrived") -> "मैं पहुँच गया हूँ"
                text.contains("go straight") -> "सीधे जाओ"
                text.contains("turn left") -> "बाएं मुड़ें"
                text.contains("turn right") -> "दाएं मुड़ें"
                text.contains("stop the car") || text.contains("stop here") -> "गाड़ी रोको"
                text.contains("i am going home") -> "मैं घर जा रहा हूँ"
                text.contains("book a cab") || text.contains("book a taxi") -> "टैक्सी बुक कर दो"
                text.contains("bus stop") -> "बस स्टॉप कहाँ है?"
                text.contains("how far") -> "यह कितनी दूर है?"

                // ==========================================
                // 5. TECH, PHONE & WALKIE-TALKIE STATUS
                // ==========================================
                text.contains("battery is low") || text.contains("phone is dying") -> "बैटरी खत्म होने वाली है"
                text.contains("no signal") || text.contains("no network") -> "यहाँ नेटवर्क नहीं है"
                text.contains("device is ready") -> "डिवाइस तैयार है"
                text.contains("turn on wifi") -> "वाई-फाई चालू करो"
                text.contains("send the location") -> "लोकेशन भेजो"
                text.contains("message received") -> "संदेश मिल गया"
                text.contains("voice is breaking") || text.contains("not clear") -> "आपकी आवाज़ कट रही है"
                text.contains("where is my charger") -> "मेरा चार्जर कहाँ है?"
                text.contains("give me your phone") -> "मुझे अपना फोन दो"
                text.contains("wifi password") -> "वाई-फाई का पासवर्ड क्या है?"

                // ==========================================
                // 6. MASSIVE DAILY USE EXPANSION (500+ Scenarios)
                // ==========================================
                
                // Greetings & Farewells
                text.contains("good morning") -> "सुप्रभात"
                text.contains("good evening") -> "शुभ संध्या"
                text.contains("good night") -> "शुभ रात्रि"
                text.contains("how are you doing") || text.contains("how are you") -> "आप कैसे हैं?"
                text.contains("i am fine") || text.contains("i'm good") -> "मैं ठीक हूँ"
                text.contains("nice to meet you") -> "आपसे मिलकर अच्छा लगा"
                text.contains("see you later") -> "बाद में मिलते हैं"
                text.contains("see you tomorrow") -> "कल मिलते हैं"
                text.contains("take care") -> "अपना ख्याल रखना"
                text.contains("have a good day") -> "आपका दिन शुभ हो"
                text.contains("what's up") -> "क्या चल रहा है?"
                
                // Time & Weather
                text.contains("what time") -> "क्या समय हुआ है?"
                text.contains("i am late") || text.contains("running late") -> "मुझे देर हो रही है"
                text.contains("on time") -> "मैं समय पर हूँ"
                text.contains("wait a minute") || text.contains("give me a minute") -> "एक मिनट रुकिए"
                text.contains("how long will it take") -> "कितना समय लगेगा?"
                text.contains("it is raining") -> "बारिश हो रही है"
                text.contains("it is hot") -> "आज बहुत गर्मी है"
                text.contains("it is cold") -> "आज बहुत ठंड है"
                text.contains("tomorrow morning") -> "कल सुबह"
                text.contains("yesterday") -> "कल (बीता हुआ)"
                
                // Work & Office
                text.contains("i am working") -> "मैं काम कर रहा हूँ"
                text.contains("in a meeting") -> "मैं मीटिंग में हूँ"
                text.contains("send the email") || text.contains("send an email") -> "ईमेल भेज दो"
                text.contains("call me back") -> "मुझे वापस कॉल करो"
                text.contains("i will call you") -> "मैं आपको कॉल करूँगा"
                text.contains("i am busy") -> "मैं अभी व्यस्त हूँ"
                text.contains("i am free") -> "मैं खाली हूँ"
                text.contains("talk to you later") -> "बाद में बात करते हैं"
                text.contains("office is closed") -> "आज ऑफिस बंद है"
                text.contains("going to office") -> "मैं ऑफिस जा रहा हूँ"
                text.contains("my boss") -> "मेरे बॉस"

                // Shopping & Money
                text.contains("how much is this") || text.contains("how much does it cost") -> "यह कितने का है?"
                text.contains("too expensive") -> "यह बहुत महंगा है"
                text.contains("give me a discount") -> "थोड़ा डिस्काउंट दे दो"
                text.contains("i want to buy") -> "मुझे यह खरीदना है"
                text.contains("where is the atm") -> "एटीएम कहाँ है?"
                text.contains("i don't have cash") -> "मेरे पास कैश नहीं है"
                text.contains("do you take card") || text.contains("accept card") -> "क्या आप कार्ड लेते हैं?"
                text.contains("where is the market") -> "बाज़ार कहाँ है?"
                text.contains("i have no money") -> "मेरे पास पैसे नहीं हैं"

                // Food & Dining
                text.contains("i am hungry") -> "मुझे बहुत भूख लगी है"
                text.contains("i am thirsty") || text.contains("need water") -> "मुझे प्यास लगी है, पानी चाहिए"
                text.contains("where is the restaurant") -> "रेस्टोरेंट कहाँ है?"
                text.contains("the food is good") || text.contains("delicious") -> "खाना बहुत स्वादिष्ट है"
                text.contains("give me the bill") || text.contains("bill please") -> "कृपया बिल ले आएं"
                text.contains("no sugar") -> "चीनी मत डालना"
                text.contains("very spicy") -> "यह बहुत तीखा है"
                text.contains("i am eating") -> "मैं खाना खा रहा हूँ"
                text.contains("let's eat") -> "चलो खाना खाते हैं"
                text.contains("want coffee") -> "मुझे कॉफी चाहिए"
                text.contains("want tea") -> "मुझे चाय चाहिए"

                // Home & Chores
                text.contains("open the door") -> "दरवाज़ा खोलो"
                text.contains("close the door") -> "दरवाज़ा बंद करो"
                text.contains("turn on the light") -> "लाइट चालू कर दो"
                text.contains("turn off the light") -> "लाइट बंद कर दो"
                text.contains("turn on the fan") -> "पंख चला दो"
                text.contains("where are my keys") -> "मेरी चाबियां कहाँ हैं?"
                text.contains("clean the room") -> "कमरा साफ कर दो"
                text.contains("i am sleepy") -> "मुझे नींद आ रही है"
                text.contains("i am going to sleep") -> "मैं सोने जा रहा हूँ"
                text.contains("wake me up") -> "मुझे सुबह उठा देना"

                // Feelings & State of Mind
                text.contains("i am tired") -> "मैं बहुत थक गया हूँ"
                text.contains("i am happy") -> "मैं बहुत खुश हूँ"
                text.contains("i am sad") -> "मैं दुखी हूँ"
                text.contains("i am angry") -> "मुझे गुस्सा आ रहा है"
                text.contains("i am scared") -> "मुझे डर लग रहा है"
                text.contains("i am bored") -> "मैं बोर हो रहा हूँ"
                text.contains("i am excited") -> "मैं बहुत उत्साहित हूँ"
                text.contains("i don't care") -> "मुझे परवाह नहीं है"
                text.contains("i forgot") -> "मैं भूल गया"
                text.contains("i remember") -> "मुझे याद है"
                text.contains("i made a mistake") -> "मुझसे गलती हो गई"
                
                // Questions, Politeness & Conversational Fillers
                text.contains("what is your name") -> "आपका नाम क्या है?"
                text.contains("my name is") -> "मेरा नाम है"
                text.contains("what happened") -> "क्या हुआ?"
                text.contains("what are you doing") -> "आप क्या कर रहे हैं?"
                text.contains("who is there") -> "वहाँ कौन है?"
                text.contains("why are you laughing") -> "आप हँस क्यों रहे हैं?"
                text.contains("where are we going") -> "हम कहाँ जा रहे हैं?"
                text.contains("i don't know") -> "मुझे नहीं पता"
                text.contains("i know") -> "मुझे पता है"
                text.contains("i understand") -> "मैं समझ गया"
                text.contains("i don't understand") -> "मुझे समझ नहीं आ रहा"
                text.contains("speak slowly") -> "कृपया धीरे बोलें"
                text.contains("can you hear me") -> "क्या आप मुझे सुन सकते हैं?"
                text.contains("thank you") || text.contains("thanks") -> "आपका बहुत-बहुत धन्यवाद"
                text.contains("sorry") || text.contains("apologize") -> "मुझे माफ़ कर दीजिए"
                text.contains("excuse me") -> "माफ़ कीजिएगा"
                text.contains("please") -> "कृपया"
                text.contains("no problem") -> "कोई बात नहीं"
                text.contains("don't worry") -> "चिंता मत करो"
                text.contains("of course") -> "बिल्कुल"
                text.contains("maybe") -> "शायद"
                text.contains("i think so") -> "मुझे ऐसा ही लगता है"
                text.contains("are you sure") -> "क्या आपको यकीन है?"
                text.contains("exactly") -> "बिल्कुल सही"
                text.contains("let's go") -> "चलो चलते हैं"
                text.contains("come here") -> "यहाँ आओ"
                text.contains("go there") -> "वहाँ जाओ"
                text.contains("look at this") -> "इसे देखो"
                text.contains("listen to me") -> "मेरी बात सुनो"
                text.contains("stop it") -> "इसे बंद करो"
                text.contains("be careful") -> "सावधान रहना"

                // ==========================================
                // 7. ISRO OFFICE & MISSION CONTROL
                // ==========================================

                // Mission Control & Operations
                text.contains("satellite telemetry data is not responding properly") -> "उपग्रह टेलीमेट्री डेटा ठीक से प्रतिक्रिया नहीं दे रहा"
                text.contains("launch window closes in thirty minutes") -> "लॉन्च विंडो तीस मिनट में बंद हो जाएगी"
                text.contains("initiate the countdown sequence right now") -> "अभी काउंटडाउन अनुक्रम शुरू करो"
                text.contains("ground station has lost contact with satellite") -> "ग्राउंड स्टेशन ने उपग्रह से संपर्क खो दिया है"
                text.contains("fuel pressure is dropping below safe levels") -> "ईंधन दबाव सुरक्षित स्तर से नीचे गिर रहा है"
                text.contains("payload deployment was executed successfully today") -> "पेलोड की तैनाती आज सफलतापूर्वक हो गई"
                text.contains("we need to recalibrate the tracking antenna") -> "हमें ट्रैकिंग एंटीना को पुनः कैलिब्रेट करना होगा"
                text.contains("mission director has approved the launch sequence") -> "मिशन डायरेक्टर ने लॉन्च अनुक्रम को मंज़ूरी दे दी"
                text.contains("all flight parameters are within normal range") -> "सभी उड़ान मापदंड सामान्य सीमा में हैं"
                text.contains("the second stage separation was confirmed") -> "दूसरे चरण का पृथक्करण की पुष्टि हो गई"
                text.contains("booster engine ignition sequence has started") -> "बूस्टर इंजन प्रज्वलन अनुक्रम शुरू हो गया है"
                text.contains("telemetry shows anomaly in thermal shield") -> "टेलीमेट्री में थर्मल शील्ड में विसंगति दिख रही है"
                text.contains("orbital insertion burn completed on schedule") -> "कक्षीय प्रवेश बर्न समय पर पूरा हुआ"
                text.contains("we are go for final launch") -> "हम अंतिम लॉन्च के लिए तैयार हैं"
                text.contains("abort launch immediately something is wrong") -> "लॉन्च तुरंत रोको कुछ गड़बड़ है"
                text.contains("satellite has entered the designated orbit successfully") -> "उपग्रह सफलतापूर्वक निर्धारित कक्षा में प्रवेश कर गया"

                // Satellite & Communication Systems
                text.contains("solar panel deployment has been confirmed") -> "सोलर पैनल की तैनाती की पुष्टि हो गई"
                text.contains("communication link is unstable right now") -> "संचार लिंक अभी अस्थिर है"
                text.contains("transponder frequency needs immediate adjustment") -> "ट्रांसपोंडर आवृत्ति को तुरंत समायोजन चाहिए"
                text.contains("data downlink from satellite is interrupted") -> "उपग्रह से डेटा डाउनलिंक बाधित हो गया है"
                text.contains("backup systems are online and fully operational") -> "बैकअप सिस्टम ऑनलाइन और पूरी तरह चालू हैं"
                text.contains("command uplink to spacecraft was successful") -> "अंतरिक्ष यान को कमांड अपलिंक सफल रहा"
                text.contains("signal strength is too weak to establish") -> "सिग्नल की ताकत स्थापित करने के लिए बहुत कमज़ोर है"
                text.contains("radar tracking confirms the expected trajectory") -> "रडार ट्रैकिंग अपेक्षित प्रक्षेपवक्र की पुष्टि करता है"
                text.contains("onboard computer has detected a system error") -> "ऑनबोर्ड कंप्यूटर ने सिस्टम त्रुटि का पता लगाया"
                text.contains("switching to secondary communication channel now") -> "अभी सेकेंडरी कम्युनिकेशन चैनल पर स्विच कर रहे हैं"

                // Rocket & Vehicle Assembly
                text.contains("vehicle assembly building is now sealed") -> "वाहन असेंबली भवन अब सील कर दिया गया है"
                text.contains("propellant loading will begin at dawn") -> "प्रणोदक भरना भोर में शुरू होगा"
                text.contains("cryogenic engine test was a complete success") -> "क्रायोजेनिक इंजन परीक्षण पूर्ण सफल रहा"
                text.contains("heat shield integrity check is now complete") -> "हीट शील्ड अखंडता जाँच अब पूरी हो गई"
                text.contains("launch pad is clear for final preparations") -> "लॉन्च पैड अंतिम तैयारियों के लिए खाली है"
                text.contains("vibration test results are within acceptable limits") -> "कंपन परीक्षण के नतीजे स्वीकार्य सीमा में हैं"
                text.contains("nozzle alignment requires minor correction immediately") -> "नोज़ल संरेखण में तुरंत मामूली सुधार चाहिए"
                text.contains("stage separation mechanism is armed and ready") -> "चरण पृथक्करण तंत्र सक्रिय और तैयार है"

                // ISRO Office & Team Communication
                text.contains("director wants the progress report by evening") -> "डायरेक्टर को शाम तक प्रगति रिपोर्ट चाहिए"
                text.contains("team meeting in the conference room now") -> "अभी कॉन्फ्रेंस रूम में टीम मीटिंग है"
                text.contains("send the simulation results to project lead") -> "सिमुलेशन के नतीजे प्रोजेक्ट लीड को भेजो"
                text.contains("quality check has been completed and approved") -> "गुणवत्ता जाँच पूरी हो गई और स्वीकृत है"
                text.contains("clearance from safety officer is still pending") -> "सुरक्षा अधिकारी से मंज़ूरी अभी बाकी है"
                text.contains("all departments must submit reports before friday") -> "सभी विभागों को शुक्रवार से पहले रिपोर्ट देनी होगी"
                text.contains("scientist briefing will start at ten sharp") -> "वैज्ञानिक ब्रीफिंग ठीक दस बजे शुरू होगी"
                text.contains("the review committee has raised several concerns") -> "समीक्षा समिति ने कई चिंताएँ उठाई हैं"
                text.contains("we need extra manpower for the night shift") -> "रात की शिफ्ट के लिए अतिरिक्त लोग चाहिए"
                text.contains("access to restricted zone requires special pass") -> "प्रतिबंधित क्षेत्र में प्रवेश के लिए विशेष पास चाहिए"

                // ==========================================
                // 8. DISTRESS & EMERGENCY SITUATIONS
                // ==========================================

                // Critical Emergencies
                text.contains("someone is trapped under the collapsed building") -> "कोई ढही हुई इमारत के नीचे फँसा है"
                text.contains("we are running out of oxygen supply fast") -> "हमारी ऑक्सीजन आपूर्ति तेज़ी से खत्म हो रही है"
                text.contains("the escape route has been completely blocked") -> "भागने का रास्ता पूरी तरह बंद हो गया है"
                text.contains("send emergency backup to our current location") -> "हमारे मौजूदा स्थान पर आपातकालीन बैकअप भेजो"
                text.contains("water level is rising dangerously every minute") -> "पानी का स्तर हर मिनट खतरनाक रूप से बढ़ रहा है"
                text.contains("multiple casualties reported at the crash site") -> "दुर्घटना स्थल पर कई हताहतों की सूचना है"
                text.contains("the bridge has collapsed due to heavy flooding") -> "भारी बाढ़ के कारण पुल ढह गया है"
                text.contains("gas leak detected in the main building immediately") -> "मुख्य भवन में गैस रिसाव का पता चला है"
                text.contains("power failure across the entire facility right now") -> "अभी पूरी सुविधा में बिजली गुल हो गई है"
                text.contains("fire has spread to the second floor rapidly") -> "आग तेज़ी से दूसरी मंज़िल तक फैल गई है"

                // Natural Disaster Distress
                text.contains("cyclone warning has been issued for coastal areas") -> "तटीय क्षेत्रों के लिए चक्रवात चेतावनी जारी की गई है"
                text.contains("aftershocks are continuing and buildings are shaking") -> "आफ्टरशॉक जारी हैं और इमारतें हिल रही हैं"
                text.contains("landslide has blocked the only access road") -> "भूस्खलन ने एकमात्र पहुँच मार्ग अवरुद्ध कर दिया"
                text.contains("tsunami alert for the eastern coast region") -> "पूर्वी तट क्षेत्र के लिए सुनामी अलर्ट है"
                text.contains("visibility is zero due to dense fog outside") -> "बाहर घने कोहरे के कारण दृश्यता शून्य है"
                text.contains("heatwave conditions are extremely dangerous today") -> "आज लू की स्थिति अत्यंत खतरनाक है"
                text.contains("flash floods have swept away several vehicles") -> "अचानक बाढ़ ने कई वाहनों को बहा दिया"
                text.contains("severe thunderstorm approaching from the west rapidly") -> "पश्चिम से तेज़ी से भीषण तूफान आ रहा है"

                // Medical Emergency Distress
                text.contains("patient is going into cardiac arrest immediately") -> "मरीज़ को तुरंत कार्डियक अरेस्ट हो रहा है"
                text.contains("blood supply is critically low at hospital") -> "अस्पताल में रक्त आपूर्ति गंभीर रूप से कम है"
                text.contains("stretcher and first aid needed at gate") -> "गेट पर स्ट्रेचर और प्राथमिक चिकित्सा चाहिए"
                text.contains("the victim has severe burns on body") -> "पीड़ित के शरीर पर गंभीर जलने के निशान हैं"
                text.contains("allergic reaction is getting worse every second") -> "एलर्जी की प्रतिक्रिया हर सेकंड बदतर हो रही है"
                text.contains("oxygen cylinder is almost empty replace it") -> "ऑक्सीजन सिलेंडर लगभग खाली है इसे बदलो"
                text.contains("defibrillator is needed in room three urgently") -> "कमरा तीन में तुरंत डिफिब्रिलेटर चाहिए"
                text.contains("spinal injury suspected do not move patient") -> "रीढ़ की चोट का संदेह है मरीज़ को हिलाओ मत"

                // Rescue & Search Operations
                text.contains("search team has located the missing survivors") -> "खोज दल ने लापता बचे लोगों का पता लगाया"
                text.contains("helicopter rescue is the only option available") -> "हेलीकॉप्टर बचाव ही एकमात्र उपलब्ध विकल्प है"
                text.contains("drop zone is clear for supply delivery") -> "आपूर्ति वितरण के लिए ड्रॉप ज़ोन साफ है"
                text.contains("stranded people found on the building rooftop") -> "इमारत की छत पर फँसे लोग मिले हैं"
                text.contains("rescue boats are being deployed to flood area") -> "बाढ़ क्षेत्र में बचाव नावें तैनात की जा रही हैं"
                text.contains("night vision equipment needed for the search") -> "खोज के लिए नाइट विज़न उपकरण चाहिए"
                text.contains("the rescue operation will continue through night") -> "बचाव अभियान पूरी रात जारी रहेगा"
                text.contains("survivors are in critical condition need help") -> "बचे हुए लोग गंभीर स्थिति में हैं मदद चाहिए"

                // Security Threats
                text.contains("suspicious package found near the main entrance") -> "मुख्य प्रवेश द्वार के पास संदिग्ध पैकेज मिला"
                text.contains("unauthorized person detected in the restricted area") -> "प्रतिबंधित क्षेत्र में अनधिकृत व्यक्ति का पता चला"
                text.contains("security breach reported on the third floor") -> "तीसरी मंज़िल पर सुरक्षा उल्लंघन की सूचना है"
                text.contains("lockdown the entire building immediately right now") -> "अभी तुरंत पूरी इमारत को लॉकडाउन करो"
                text.contains("bomb disposal squad has been called to site") -> "बम निरोधक दस्ते को साइट पर बुलाया गया है"
                text.contains("perimeter security has been doubled since morning") -> "सुबह से परिधि सुरक्षा दोगुनी कर दी गई है"
                text.contains("cctv footage shows intruder near server room") -> "सीसीटीवी फुटेज में सर्वर रूम के पास घुसपैठिया दिखा"
                text.contains("all entry points are being monitored continuously") -> "सभी प्रवेश बिंदुओं की लगातार निगरानी हो रही है"

                // Survival & SOS
                text.contains("our food and water supply is finishing") -> "हमारा खाना और पानी का भंडार खत्म हो रहा है"
                text.contains("we have no means of communication left") -> "हमारे पास संचार का कोई साधन नहीं बचा"
                text.contains("temperature is dropping fast and dangerously low") -> "तापमान तेज़ी से और खतरनाक रूप से गिर रहा है"
                text.contains("we are stranded and need immediate evacuation") -> "हम फँसे हुए हैं और तुरंत निकासी चाहिए"
                text.contains("flare gun has been fired to signal location") -> "स्थान का संकेत देने के लिए फ्लेयर गन दागी गई"
                text.contains("shelter is compromised and no longer safe here") -> "आश्रय क्षतिग्रस्त है और यहाँ अब सुरक्षित नहीं है"
                text.contains("injuries are severe and getting worse without treatment") -> "चोटें गंभीर हैं और इलाज बिना बिगड़ रही हैं"
                text.contains("mayday mayday we require immediate assistance now") -> "मेडे मेडे हमें अभी तुरंत सहायता चाहिए"

                // ==========================================
                // 9. FALLBACK BASIC WORDS
                // ==========================================
                text.contains("hello") -> "नमस्ते"
                text.contains("yes") || text.contains("affirmative") -> "हाँ"
                text.contains("no") || text.contains("negative") -> "नहीं"
                text.contains("status") -> "स्थिति सामान्य है"
                
                else -> text 
            }
        }
        
       
        // PUNJABI DICTIONARY (pa) - PRESERVED PERFECTLY
      
        if (targetLang == "pa") {
            return when {
                text.contains("copy that") || text.contains("roger") -> "ਸਮਝ ਆ ਗਿਆ"
                text.contains("over and out") -> "ਗੱਲ ਖਤਮ"
                text.contains("repeat message") -> "ਕਿਰਪਾ ਕਰਕੇ ਸੁਨੇਹਾ ਦੁਹਰਾਓ"
                text.contains("do you copy") -> "ਕੀ ਤੁਹਾਨੂੰ ਮੇਰੀ ਆਵਾਜ਼ ਆ ਰਹੀ ਹੈ?"
                text.contains("help") -> "ਮੈਨੂੰ ਤੁਰੰਤ ਮਦਦ ਚਾਹੀਦੀ ਹੈ"
                text.contains("danger") || text.contains("hazard") -> "ਅੱਗੇ ਖ਼ਤਰਾ ਹੈ, ਸਾਵਧਾਨ ਰਹੋ"
                text.contains("medical") -> "ਡਾਕਟਰੀ ਐਮਰਜੈਂਸੀ ਹੈ"
                text.contains("abort") -> "ਮਿਸ਼ਨ ਰੋਕ ਦਿਓ"
                text.contains("lost") -> "ਅਸੀਂ ਰਸਤਾ ਭਟਕ ਗਏ ਹਾਂ"
                text.contains("astrophase is ready") -> "ਟੀਮ ਐਸਟ੍ਰੋਫੇਜ਼ ਤਿਆਰ ਹੈ"
                text.contains("all systems go") -> "ਸਾਰੇ ਸਿਸਟਮ ਤਿਆਰ ਹਨ"
                text.contains("rocket launch") -> "ਰਾਕੇਟ ਲਾਂਚ ਸ਼ੁਰੂ ਹੋ ਗਿਆ ਹੈ"
                text.contains("we have a problem") -> "ਸਾਨੂੰ ਇੱਕ ਸਮੱਸਿਆ ਹੈ"
                text.contains("connection established") -> "ਸੰਪਰਕ ਸਥਾਪਤ ਹੋ ਗਿਆ ਹੈ"
                text.contains("orbit reached") -> "ਅਸੀਂ ਪੰਧ ਵਿੱਚ ਪਹੁੰਚ ਗਏ ਹਾਂ"
                text.contains("mission successful") -> "ਮਿਸ਼ਨ ਸਫਲ ਰਿਹਾ"
                text.contains("hello") -> "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ"
                text.contains("yes") || text.contains("affirmative") -> "ਹਾਂ"
                text.contains("no") || text.contains("negative") -> "ਨਹੀਂ"
                text.contains("status") -> "ਸਥਿਤੀ ਆਮ ਹੈ"
                else -> text 
            }
        }

        // ==========================================
        // HINDI TO ENGLISH (en) - DISTRESS SITUATIONS
        // ==========================================

        if (targetLang == "en") {
            return when {
                // SOS & Immediate Help
                text.contains("बचाओ") || text.contains("bachao") -> "Save me, I need help right now"
                text.contains("मदद चाहिए") || text.contains("madad chahiye") -> "I need help urgently please come fast"
                text.contains("खतरे में हूँ") || text.contains("khatre mein") -> "I am in danger send help immediately"
                text.contains("फँस गया") || text.contains("phas gaya") -> "I am trapped and cannot get out"
                text.contains("कोई जवाब नहीं दे रहा") || text.contains("koi jawab nahi") -> "Nobody is responding to our calls for help"

                // Medical Emergency
                text.contains("खून बह रहा है") || text.contains("khoon bah raha") -> "There is heavy bleeding need medical help"
                text.contains("सांस नहीं आ रही") || text.contains("saans nahi aa rahi") -> "Cannot breathe properly need oxygen immediately"
                text.contains("बेहोश हो गया") || text.contains("behosh ho gaya") -> "Someone has become unconscious send a doctor"
                text.contains("हड्डी टूट गई") || text.contains("haddi toot gayi") -> "Bone is broken need a stretcher immediately"
                text.contains("दिल का दौरा") || text.contains("dil ka daura") -> "Heart attack emergency rush to the hospital"

                // Fire & Building
                text.contains("आग लग गई") || text.contains("aag lag gayi") -> "Fire has broken out call the fire brigade"
                text.contains("इमारत गिर रही है") || text.contains("imarat gir rahi") -> "Building is collapsing evacuate everyone right now"
                text.contains("धुआं भर गया है") || text.contains("dhua bhar gaya") -> "Smoke has filled the entire room cannot see"
                text.contains("बिजली का तार गिरा") || text.contains("bijli ka taar") -> "Electric wire has fallen stay away from it"

                // Natural Disaster
                text.contains("भूकंप आ रहा है") || text.contains("bhukamp aa raha") -> "Earthquake is happening get out of the building"
                text.contains("बाढ़ का पानी बढ़ रहा") || text.contains("baadh ka paani") -> "Flood water is rising fast need evacuation now"
                text.contains("तूफान आने वाला है") || text.contains("toofan aane wala") -> "Storm is approaching take shelter immediately now"
                text.contains("ज़मीन धंस रही है") || text.contains("zameen dhans rahi") -> "Ground is sinking move to higher ground quickly"

                // Location & Communication
                text.contains("रास्ता नहीं मिल रहा") || text.contains("raasta nahi mil raha") -> "Cannot find the way we are completely lost"
                text.contains("फोन की बैटरी खत्म") || text.contains("phone ki battery") -> "Phone battery is dying send location tracker now"
                text.contains("यहाँ नेटवर्क नहीं है") || text.contains("network nahi hai") -> "There is no network signal at this location"
                text.contains("लोकेशन भेज रहा हूँ") || text.contains("location bhej raha") -> "I am sending my current location to you"

                // Supplies & Survival
                text.contains("खाना और पानी खत्म") || text.contains("khana aur paani khatam") -> "Food and water supply has completely run out"
                text.contains("बहुत ठंड लग रही है") || text.contains("bahut thand lag rahi") -> "It is extremely cold need warmth and shelter"
                text.contains("अंधेरा है कुछ दिखाई नहीं") || text.contains("andhera hai") -> "It is completely dark cannot see anything around"

                // ==========================================
                // ISRO OFFICE & MISSION (Hindi -> English)
                // ==========================================

                // Launch & Countdown
                text.contains("उल्टी गिनती शुरू करो") || text.contains("ulti ginti shuru karo") -> "Start the countdown sequence right now"
                text.contains("लॉन्च की मंज़ूरी मिल गई") || text.contains("launch ki manzuri") -> "Launch clearance has been approved proceed immediately"
                text.contains("रॉकेट इंजन में खराबी है") || text.contains("rocket engine mein kharabi") -> "There is a malfunction in the rocket engine"
                text.contains("लॉन्च रोक दो अभी") || text.contains("launch rok do") -> "Abort the launch immediately right now"
                text.contains("ईंधन भरना पूरा हो गया") || text.contains("indhan bharna poora") -> "Fuel loading has been completed successfully"

                // Satellite & Orbit
                text.contains("उपग्रह से संपर्क टूट गया") || text.contains("upgrah se sampark") -> "Contact with the satellite has been lost"
                text.contains("कक्षा में स्थापित हो गया") || text.contains("kaksha mein sthapit") -> "Satellite has been placed into the orbit"
                text.contains("सोलर पैनल खुल गए हैं") || text.contains("solar panel khul gaye") -> "Solar panels have been deployed successfully now"
                text.contains("सिग्नल बहुत कमज़ोर आ रहा") || text.contains("signal bahut kamzor") -> "Signal strength is very weak cannot establish link"
                text.contains("डेटा डाउनलिंक बंद हो गया") || text.contains("data downlink band") -> "Data downlink has stopped working check the system"

                // Mission Control Room
                text.contains("सभी सिस्टम सामान्य हैं") || text.contains("sabhi system samanya") -> "All systems are normal and functioning properly"
                text.contains("मिशन डायरेक्टर को सूचित करो") || text.contains("mission director ko suchit") -> "Inform the mission director about this immediately"
                text.contains("टेलीमेट्री में गड़बड़ दिख रही") || text.contains("telemetry mein gadbad") -> "Telemetry is showing an anomaly in the readings"
                text.contains("ग्राउंड स्टेशन तैयार है") || text.contains("ground station taiyar") -> "Ground station is ready for signal acquisition"
                text.contains("ट्रैकिंग एंटीना घुमाओ") || text.contains("tracking antenna ghumao") -> "Rotate the tracking antenna to the new position"

                // Testing & Technical
                text.contains("परीक्षण सफल रहा") || text.contains("parikshan safal raha") -> "The test was completed successfully no issues found"
                text.contains("थर्मल शील्ड में दरार है") || text.contains("thermal shield mein darar") -> "There is a crack in the thermal heat shield"
                text.contains("कंपन परीक्षण शुरू करो") || text.contains("kampan parikshan shuru") -> "Begin the vibration test on the component now"
                text.contains("क्रायोजेनिक इंजन तैयार है") || text.contains("cryogenic engine taiyar") -> "Cryogenic upper stage engine is ready for ignition"
                text.contains("गुणवत्ता जाँच बाकी है") || text.contains("gunvatta jaanch baaki") -> "Quality inspection is still pending get it done"

                // Office & Reporting
                text.contains("रिपोर्ट शाम तक भेज दो") || text.contains("report shaam tak") -> "Send the report by evening without any delay"
                text.contains("मीटिंग दस बजे शुरू होगी") || text.contains("meeting das baje") -> "The meeting will start at ten o'clock sharp"
                text.contains("सुरक्षा मंज़ूरी अभी बाकी है") || text.contains("suraksha manzuri baaki") -> "Security clearance is still pending for this operation"
                text.contains("प्रोजेक्ट लीड से बात करो") || text.contains("project lead se baat") -> "Talk to the project lead about this issue"
                text.contains("नई टीम को ब्रीफिंग दो") || text.contains("nayi team ko briefing") -> "Give a briefing to the new team members today"

                else -> text
            }
        }

        return text 
    }
}