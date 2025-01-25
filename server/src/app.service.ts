import { Injectable, InternalServerErrorException } from '@nestjs/common';
import { ExplainFunctionResponse } from './explain-function.response.dto';
import { ExplainFunctionRequest } from './explain-function.request.dto';
import OpenAI from 'openai';
import { FunctionExplanationSchema } from './function-explanation.schema';

@Injectable()
export class AppService {
  private openAI: OpenAI;
  private prompt = `
The following json object is a step by step explanation of a java function. 
Please generate a PlantUml code of this json object. 
Please ensure that the generate PlantUML  code uses the exact same text for each step in the PlantUml as it is in each step in the json object

{{json_object}}
`;

  private jsonPrompt = `
You are a Java code explainer. Given a Java function, provide a detailed step-by-step explanation of what the function does. The explanation should be structured in the following JSON format:

{
  "functionName": "The function name",
  "functionParams": ["list of function arguments"],
  "explanation": [
    "Step 1: Explanation of the first action or logic the function performs.",
    "Step 2: Explanation of the next action, and so on, continuing through all relevant steps."
  ]
}

Please ensure to provide your response as a JSON object with the following schema: 
{{schema}}

Java Function: 
{{java_function}}
`;

  constructor() {
    this.openAI = new OpenAI({
      apiKey: process.env['OPENAI_API_KEY'],
    });
  }

  async explainFunction(
    explainFunctionRequest: ExplainFunctionRequest,
  ): Promise<ExplainFunctionResponse> {
    const variables: Map<string, string> = new Map<string, string>();
    variables['java_function'] = explainFunctionRequest.function;
    const schema = JSON.stringify(FunctionExplanationSchema);
    variables['schema'] = schema;

    const generatedJsonPrompt = this.generatePrompt(this.jsonPrompt, variables);
    const json = await this.runPrompt(generatedJsonPrompt);
    console.log(json);

    variables['json_object'] = json;

    const generatedPrompt = this.generatePrompt(this.prompt, variables);
    const uml = await this.runPrompt(generatedPrompt);
    const formatedUml = this.formatUmlResponse(uml);
    return { uml };
  }

  private async runPrompt(prompt: string): Promise<string> {
    //const tokenCount = 4097 - (await this.determineMaxTokens(prompt));
    try {
      const response = await this.openAI.chat.completions.create({
        messages: [{ role: 'user', content: prompt }],
        model: 'gpt-4',
      });
      const content = response.choices[0]?.message?.content;
      return content;
    } catch (e) {
      console.log(e.response.data.error);
      throw new InternalServerErrorException('Error running prompt');
    }
  }

  private formatUmlResponse(content: string): string {
    let formattedResponse = content.trim();
    if (
      formattedResponse.startsWith('```') &&
      formattedResponse.endsWith('```')
    ) {
      formattedResponse = formattedResponse.slice(3, -3).trim();
    }

    formattedResponse = formattedResponse.replace(/\\n/g, '\n');

    const startUMLIndex = formattedResponse.indexOf('@startuml');

    formattedResponse = formattedResponse.slice(startUMLIndex);

    console.log(formattedResponse);
    return formattedResponse;
  }

  private determineMaxTokens(prompt: string): number {
    const tokenCount = this.countTokens(prompt);
    // Adjust the value as needed, considering API limitations, response length, and other factors
    const maxTokens = tokenCount + 10; // Add a buffer of 10 tokens
    return maxTokens;
  }

  private countTokens(text: string): number {
    const words = text.trim().split(/\s+/);
    const punctuation = text.replace(/\w+/g, '');
    const tokenCount = words.length + punctuation.length;
    return tokenCount;
  }

  private generatePrompt(
    template: string,
    variables: Map<string, string>,
  ): string {
    // Replace variables within the template
    let generatedString = template.replace(
      /{{(\w+)}}/g,
      (match, variableKey) => {
        if (variables.hasOwnProperty(variableKey)) {
          return variables[variableKey];
        }
        return match; // Keep the original placeholder if the variable is not provided
      },
    );

    return generatedString;
  }
}
