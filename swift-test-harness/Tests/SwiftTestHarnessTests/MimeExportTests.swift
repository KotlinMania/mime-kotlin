import Testing
import Mime

// Smoke test for the Kotlin -> Swift Export -> SPM -> swift test pipeline.
@Suite("Mime Export Tests")
struct MimeExportTests {
    @Test("Swift module loads and imports cleanly")
    func swiftModuleLoads() {
        #expect(true)
    }
}

