export const FunctionExplanationSchema = {
  type: 'object',
  properties: {
    functionName: {
      type: 'string',
    },
    functionParams: {
      type: 'array',
      items: [
        {
          type: 'string',
        },
      ],
    },
    explanation: {
      type: 'array',
      items: [
        {
          type: 'string',
        },
      ],
    },
  },
  required: ['functionName', 'functionParams', 'explanation'],
};
