import { describe, it, expect } from 'vitest'
import { render } from './__test__/utils.tsx'
import App from './App'

describe('App', () => {
    it('renders without crashing', () => {
        render(<App />)
        expect(document.body).toBeInTheDocument()
    })
})