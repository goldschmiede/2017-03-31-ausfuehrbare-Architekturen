package base

import medical.external.FileStore
import spock.lang.Specification

class FilestoreTests extends Specification {

    def "Filestore works as expected" () {
        given:
        def files = [
                'mnt': [
                        'documents': [
                                'import': ['file1.xls': '123456', 'file2.xls': '00000', 'file3.docx':'tttttttx'],
                                'cases': [
                                        'N16.1':['file22.xls': '22221', 'file.txt': 'txttxttxt'],
                                        'N16.2':['file-1.bam': '01001010111010', 'file-2.pdf': 'lllll']
                                ]
                        ]
                ]
        ]
        def filestore = new FileStore(files: files)

        when:
        def dir1 = filestore.listDirectory('/mnt/documents/import')

        then:
        dir1.size() == 3
        dir1['file1.xls'] == '123456'
        dir1['file2.xls'] == '00000'

        when:
        filestore.writeFile('/mnt/documents/import', 'newfile.txt', 'aaaaa')

        then:
        filestore.listDirectory('/mnt/documents/import').size() == 4

        and:
        filestore.listDirectory('/mnt/documents/import')['newfile.txt'] == 'aaaaa'

        when:
        filestore.moveFile('/mnt/documents/import', '/mnt/documents/cases/N16.1', 'newfile.txt')

        then:
        filestore.listDirectory('/mnt/documents/import').size() == 3

        and:
        filestore.listDirectory('/mnt/documents/cases/N16.1').size() == 3

        and:
        filestore.listDirectory('/mnt/documents/cases/N16.1')['newfile.txt'] == 'aaaaa'

        when:
        filestore.createDirectory('/mnt/documents/cases', 'N16.22')

        and:
        filestore.moveFile('/mnt/documents/import', '/mnt/documents/cases/N16.22', 'file1.xls')

        then:
        filestore.listDirectory('/mnt/documents/cases/N16.22')['file1.xls'] == '123456'


    }
}
