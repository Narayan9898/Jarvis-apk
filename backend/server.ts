import express, { Request, Response } from 'express';
import rateLimit from 'express-rate-limit';
import helmet from 'helmet';
import cors from 'cors';
import dotenv from 'dotenv';
import { GoogleGenAI } from '@google/genai';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 8080;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const CLIENT_AUTH_TOKEN = process.env.CLIENT_AUTH_TOKEN || 'jarvis-secure-token';

if (!GEMINI_API_KEY) {
  console.warn("WARNING: GEMINI_API_KEY environment variable is not defined!");
}

const ai = new GoogleGenAI({ apiKey: GEMINI_API_KEY || '' });

// Security Middleware
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '256kb' }));

// Rate limiting: 60 requests per minute per IP
const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 60,
  message: { error: 'Rate limit exceeded. Please wait a moment.' }
});
app.use('/api/', apiLimiter);

// Bearer Token Authentication
const authenticateClient = (req: Request, res: Response, next: Function) => {
  const authHeader = req.headers.authorization;
  if (CLIENT_AUTH_TOKEN && authHeader !== `Bearer ${CLIENT_AUTH_TOKEN}`) {
    return res.status(401).json({ error: 'Unauthorized: Invalid bearer credentials.' });
  }
  next();
};

const SYSTEM_INSTRUCTION = `
You are JARVIS (Just A Rather Very Intelligent System), an advanced personal AI assistant on an Android mobile device.
Your tone is polite, witty, sophisticated, respectful, and highly competent.
You support English, Telugu, Hindi, and Tanglish (Telugu-English blend).
Always respond to the user in the language they used or are most comfortable with.

When the user requests an action, formulate a structured action proposal:
- open_app: parameters {"app_name": "...", "package_name": "..."}
- search_web: parameters {"query": "..."}
- open_maps: parameters {"destination": "..."}
- open_youtube: parameters {"query": "..."}
- dial_phone: parameters {"target": "..."}
- send_sms: parameters {"recipient": "...", "message": "..."}
- create_reminder: parameters {"title": "...", "minutes_from_now": "..."}
- set_alarm: parameters {"hour": "...", "minute": "...", "message": "..."}
- set_timer: parameters {"seconds": "...", "message": "..."}
- open_settings: parameters {"setting_type": "wifi|bluetooth|battery|display|accessibility|sound|apps"}
- open_camera: parameters {}
- open_whatsapp: parameters {"phone_number": "...", "text": "..."}
- save_memory: parameters {"key": "...", "value": "..."}
- summarize_notifications: parameters {}

Respond STRICTLY in JSON format:
{
  "response": "<conversational spoken reply>",
  "action": {
    "actionId": "<actionId or null>",
    "parameters": { ... }
  }
}
If no device action is needed, set "action": null.
`;

app.post('/api/chat', authenticateClient, async (req: Request, res: Response) => {
  try {
    const { query, history } = req.body;
    if (!query || typeof query !== 'string') {
      return res.status(400).json({ error: 'Field "query" is required.' });
    }

    const contents: any[] = [];
    if (Array.isArray(history)) {
      for (const turn of history.slice(-4)) {
        contents.push({
          role: turn.role === 'user' ? 'user' : 'model',
          parts: [{ text: turn.text }]
        });
      }
    }
    contents.push({ role: 'user', parts: [{ text: query }] });

    const response = await ai.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: contents,
      config: {
        systemInstruction: SYSTEM_INSTRUCTION,
        responseMimeType: 'application/json',
        temperature: 0.4
      }
    });

    const responseText = response.text || '{}';
    return res.json(JSON.parse(responseText));
  } catch (err: any) {
    console.error('Gemini proxy error:', err);
    return res.status(500).json({
      error: 'Proxy AI engine error',
      details: err.message
    });
  }
});

app.get('/health', (req: Request, res: Response) => {
  res.json({ status: 'HEALTHY', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`JARVIS Secure Backend running on port ${PORT}`);
});
