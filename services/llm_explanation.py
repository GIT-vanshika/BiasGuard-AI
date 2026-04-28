import os
import json
try:
    from google import genai
except ImportError:
    genai = None

def generate_summary_and_recommendation(metrics_json: dict, proxy_features: list) -> dict:
    fallback_response = {
        "executive_summary": "Template: Bias threshold exceeded. Manual review required.",
        "counterfactual_insight": "If proxy features are removed, disparity may be reduced.",
        "fallback_used": True
    }
    
    api_key = "AIzaSyBgZuokE5wE3MgYlThmjIOy1Upfvc6BiQY"
    if not api_key or not genai:
        return fallback_response
        
    try:
        client = genai.Client(api_key=api_key)
        
        prompt = f"""
        Analyze this Bias Audit outcome and reply ONLY in valid JSON format.
        Do NOT include markdown formatting or backticks around the JSON.
        
        Metrics Payload: {json.dumps(metrics_json)}
        Proxy Features detected: {proxy_features}
        
        Provide a JSON response with two keys:
        "executive_summary": A 1-2 sentence summary explaining who is harmed and why.
        "counterfactual_insight": A 1 sentence stating what structural change might fix it based on the proxy features.
        """
        
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt,
        )
        
        text_output = response.text.replace("```json", "").replace("```", "").strip()
        parsed_json = json.loads(text_output)
        
        return {
            "executive_summary": parsed_json.get("executive_summary", fallback_response["executive_summary"]),
            "counterfactual_insight": parsed_json.get("counterfactual_insight", fallback_response["counterfactual_insight"]),
            "fallback_used": False
        }
    except Exception as e:
        print(f"Gemini generation error: {e}")
        return fallback_response
